package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentrefresh.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.exception.RasCountProcessingFailedException;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.Count;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.EmailData;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.RefreshJob;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.EmailService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.UserCountService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.SendJobDetailsService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class RefreshJobsOrchestrator {
    public static final String EMAIL_SUBJECT = "Refresh Job Task";

    private final PersistenceService persistenceService;
    private final SendJobDetailsService jobDetailsService;
    private final UserCountService userCountService;
    private final EmailService emailService;

    @Value("${refresh-job-delay-duration}")
    private long refreshJobDelayDuration;

    @Value("${refresh-job-count-delay-duration}")
    private long refreshJobCountDelayDuration;

    @Autowired
    public RefreshJobsOrchestrator(PersistenceService persistenceService,
                                   SendJobDetailsService jobDetailsService,
                                   UserCountService userCountService,
                                   EmailService emailService) {
        this.persistenceService = persistenceService;
        this.jobDetailsService = jobDetailsService;
        this.userCountService = userCountService;
        this.emailService = emailService;

    }

    public void processRefreshJobs() {
        final long startTime = System.currentTimeMillis();

        // Get new job entries for refresh
        List<RefreshJobEntity> jobs =  persistenceService.getNewJobs();

        if (jobs.isEmpty()) {
            log.info("No NEW refresh jobs found to execute");

            // NB: always call RAS User Count at least once
            triggerRASUserCount();

        } else {
            log.info("Calling RAS User Count Before Refresh");
            final ResponseEntity<Object> responseEntityBeforeRefresh = triggerRASUserCount();

            for (RefreshJobEntity job : jobs) {
                runRefreshJob(job);

                // refresh job is an async call, delay to keep the rd calls apart
                if (refreshJobDelayDuration > 0) {
                    refreshJobDelay(refreshJobDelayDuration);
                }
            }

            // delay here to ensure the refresh is completed before the count is calculated
            refreshJobDelay(refreshJobCountDelayDuration);

            log.info("Calling RAS User Count After Refresh");
            final ResponseEntity<Object> responseEntityAfterRefresh = triggerRASUserCount();

            if (responseEntityBeforeRefresh.getBody() != null && responseEntityAfterRefresh.getBody() != null) {
                final String responseBeforeRefresh = responseEntityBeforeRefresh.getBody().toString();
                final String responseAfterRefresh = responseEntityAfterRefresh.getBody().toString();
                List<RefreshJob> refreshJobs = populateRefreshJobs(jobs);

                sendEmailWithCounts(responseBeforeRefresh, responseAfterRefresh, refreshJobs);
            }

        }

        log.info(" >> Refresh Batch Job({}) execution finished at {} . Time taken = {} milliseconds",
                jobs.size(), System.currentTimeMillis(), Math.subtractExact(System.currentTimeMillis(), startTime)
        );
    }

    public List<RefreshJob> populateRefreshJobs(List<RefreshJobEntity> jobs) {
        return jobs.stream()
                .map(jobEntity -> RefreshJob.builder()
                        .jobId(jobEntity.getJobId().toString())
                        .jurisdiction(jobEntity.getJurisdiction())
                        .roleCategory(jobEntity.getRoleCategory())
                        .build())
                .toList();
    }

    private void runRefreshJob(RefreshJobEntity job) {
        if (Objects.isNull(job.getLinkedJobId()) || job.getLinkedJobId().equals(0L)) {
            log.info("Trigger refresh job with ID = {}", job.getJobId());
            sendJobToORMService(job.getJobId(), UserRequest.builder().build());
        } else {
            RefreshJobEntity linkedJob = persistenceService.getByJobId(job.getLinkedJobId());
            if (Objects.nonNull(linkedJob) && ArrayUtils.isNotEmpty(linkedJob.getUserIds())) {
                log.info("Trigger refresh job with ID = {}", job.getJobId());
                sendJobToORMService(job.getJobId(), UserRequest.builder().userIds(linkedJob.getUserIds()).build());
            } else {
                log.error("Skipping refresh job with ID = {} as issue with linked job", job.getJobId());
            }
        }
    }

    private void refreshJobDelay(final long refreshJobDelayDuration) {
        try {
            Thread.sleep(refreshJobDelayDuration);
        } catch (InterruptedException e) {
            log.error("Refresh batch delay interrupted whilst executing refresh batch job");
            Thread.currentThread().interrupt();
        }
    }

    private void sendJobToORMService(Long jobId, UserRequest userRequest) {
        ResponseEntity<Object> responseEntity =  jobDetailsService.sendToRoleAssignmentBatchService(jobId, userRequest);
        if (responseEntity.getStatusCode() != HttpStatus.ACCEPTED) {
            throw new UnprocessableEntityException(responseEntity.toString());
        }
    }

    public ResponseEntity<Object> triggerRASUserCount() {
        ResponseEntity<Object> responseEntity =  userCountService.getRasUserCounts();
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            log.error("Error return from RAS User Count: " + responseEntity.toString());
        }
        return responseEntity;
    }


    public List<Count> compareCounts(String before, String after, String countName) {
        ObjectMapper mapper = new ObjectMapper();
        List<Count> countOutput = new ArrayList<>();

        try {
            JsonNode beforeObj = mapper.readTree(before);
            JsonNode afterObj = mapper.readTree(after);

            JsonNode beforeJurisdiction = beforeObj.get(countName);
            JsonNode afterJurisdiction = afterObj.get(countName);

            if (beforeJurisdiction != null) {
                // pre populate the count output from the before count
                for (int x = 0; x < beforeJurisdiction.size(); x++) {
                    JsonNode currentNode = beforeJurisdiction.get(x);
                    Count count = new Count();
                    count.populateBefore(currentNode);
                    countOutput.add(count);
                }
            }

            if (afterJurisdiction != null) {
                // merge the after count into the count output
                for (int i = 0; i < afterJurisdiction.size(); i++) {
                    JsonNode currentNode = afterJurisdiction.get(i);
                    Count count = new Count();
                    count.populateAfter(currentNode);
                    updateWithAfterCountOrCreateNew(count, countOutput);
                }
            }


        } catch (JsonProcessingException e) {
            throw new RasCountProcessingFailedException("An error occurred while processing RAS Count JSON",e);
        }

        countOutput.sort(Comparator
                .comparing(Count::getJurisdiction, Comparator.nullsFirst(String::compareTo))
                .thenComparing(Count::getRoleCategory, Comparator.nullsFirst(String::compareTo))
                .thenComparing(Count::getRoleName, Comparator.nullsFirst(String::compareTo)));

        return countOutput;
    }

    public Count findMatchingCount(Count needle, List<Count> haystack) {
        Count returnCount = null;

        for (Count count : haystack) {
            boolean jurisdictionMatch = isJurisdictionMatch(count, needle);
            boolean roleNameMatch = isRoleNameMatch(count, needle);
            boolean roleCategoryMatch = count.getRoleCategory().equals(needle.getRoleCategory());

            if (jurisdictionMatch && roleCategoryMatch && roleNameMatch) {
                returnCount = count;
            }
        }
        return returnCount;
    }

    boolean isJurisdictionMatch(Count count, Count needle) {
        if (count.getJurisdiction() == null && needle.getJurisdiction() == null) {
            return true;
        } else if (count.getJurisdiction() != null && needle.getJurisdiction() != null) {
            return count.getJurisdiction().equals(needle.getJurisdiction());
        } else {
            return false;
        }
    }

    boolean isRoleNameMatch(Count count, Count needle) {
        if (count.getRoleName() == null && needle.getRoleName() == null) {
            return true;
        } else if (count.getRoleName() != null && needle.getRoleName() != null) {
            return count.getRoleName().equals(needle.getRoleName());
        } else {
            return false;
        }
    }

    public void updateWithAfterCountOrCreateNew(Count afterCount, List<Count> counts) {
        Count matchedCount = findMatchingCount(afterCount, counts);
        if (matchedCount != null) {
            matchedCount.updateAfterCount(afterCount.getAfterCount());
        } else {
            afterCount.setDifference(afterCount.getAfterCount());
            counts.add(afterCount);
        }
    }

    public void sendEmailWithCounts(String responseBeforeRefresh, String responseAfterRefresh,
                                    List<RefreshJob> refreshJobs) {
        List<Count> jurisdictionCount = compareCounts(responseBeforeRefresh, responseAfterRefresh,
                "OrgUserCountByJurisdiction");
        List<Count> jurisdictionAndRoleNameCount = compareCounts(responseBeforeRefresh, responseAfterRefresh,
                "OrgUserCountByJurisdictionAndRoleName");

        Map<String, Object> templateMap = new HashMap<>();

        templateMap.put("refreshJobs", refreshJobs);
        templateMap.put("jurisdictionCount", jurisdictionCount);
        templateMap.put("jurisdictionAndRoleNameCount", jurisdictionAndRoleNameCount);

        EmailData emailData = EmailData
                .builder()
                .emailSubject(EMAIL_SUBJECT)
                .templateMap(templateMap)
                .build();
        emailService.sendEmail(emailData);
    }
}
