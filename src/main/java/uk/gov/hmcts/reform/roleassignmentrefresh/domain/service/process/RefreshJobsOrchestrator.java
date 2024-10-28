package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentrefresh.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.Count;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.CountResponse;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.EmailData;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.RefreshJob;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.EmailService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.UserCountService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.SendJobDetailsService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    public static final String ORG_USER_COUNT_BY_JURISDICTION = "OrgUserCountByJurisdiction";
    public static final String ORG_USER_COUNT_BY_JURISDICTION_AND_ROLE_NAME = "OrgUserCountByJurisdictionAndRoleName";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss z Z";

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
            String beforeRefreshTime = ZonedDateTime.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
            final ResponseEntity<CountResponse> responseEntityBeforeRefresh = triggerRASUserCount();

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
            final ResponseEntity<CountResponse> responseEntityAfterRefresh = triggerRASUserCount();
            String afterRefreshTime = ZonedDateTime.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));

            if (responseEntityBeforeRefresh.getBody() != null && responseEntityAfterRefresh.getBody() != null) {
                final CountResponse responseBeforeRefresh = responseEntityBeforeRefresh.getBody();
                final CountResponse responseAfterRefresh = responseEntityAfterRefresh.getBody();
                List<RefreshJob> refreshJobs = populateRefreshJobs(jobs);

                sendEmailWithCounts(responseBeforeRefresh, responseAfterRefresh, refreshJobs, beforeRefreshTime,
                        afterRefreshTime);
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

    public ResponseEntity<CountResponse> triggerRASUserCount() {
        ResponseEntity<CountResponse> responseEntity =  userCountService.getRasUserCounts();
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            log.error("Error return from RAS User Count: " + responseEntity.toString());
        }
        return responseEntity;
    }


    public List<Count> compareCounts(CountResponse before, CountResponse after, String countName) {
        List<Count> countOutput = new ArrayList<>();

        List<CountResponse.CountData> beforeList;
        if (ORG_USER_COUNT_BY_JURISDICTION.equals(countName)) {
            beforeList = before.getOrgUserCountByJurisdiction() != null
                    ? List.of(before.getOrgUserCountByJurisdiction()) : new ArrayList<>();
        } else {
            beforeList = before.getOrgUserCountByJurisdictionAndRoleName() != null
                    ? List.of(before.getOrgUserCountByJurisdictionAndRoleName()) : new ArrayList<>();
        }

        for (CountResponse.CountData countData : beforeList) {
            Count count = new Count();
            count.populateBefore(countData);
            countOutput.add(count);
        }

        List<CountResponse.CountData> afterList;
        if (ORG_USER_COUNT_BY_JURISDICTION.equals(countName)) {
            afterList = after.getOrgUserCountByJurisdiction() != null
                    ? List.of(after.getOrgUserCountByJurisdiction()) : new ArrayList<>();
        } else {
            afterList = after.getOrgUserCountByJurisdictionAndRoleName() != null
                    ? List.of(after.getOrgUserCountByJurisdictionAndRoleName()) : new ArrayList<>();
        }

        for (CountResponse.CountData countData : afterList) {
            Count count = new Count();
            count.populateAfter(countData);
            updateWithAfterCountOrCreateNew(count, countOutput);
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

    public void sendEmailWithCounts(CountResponse responseBeforeRefresh, CountResponse responseAfterRefresh,
                                    List<RefreshJob> refreshJobs, String beforeRefreshTime, String afterRefreshTime) {
        List<Count> jurisdictionCount = compareCounts(responseBeforeRefresh, responseAfterRefresh,
                ORG_USER_COUNT_BY_JURISDICTION);
        List<Count> jurisdictionAndRoleNameCount = compareCounts(responseBeforeRefresh, responseAfterRefresh,
                ORG_USER_COUNT_BY_JURISDICTION_AND_ROLE_NAME);

        Map<String, Object> templateMap = new HashMap<>();

        templateMap.put("refreshJobs", refreshJobs);
        templateMap.put("jurisdictionCount", jurisdictionCount);
        templateMap.put("jurisdictionAndRoleNameCount", jurisdictionAndRoleNameCount);
        templateMap.put("beforeRefreshTime", beforeRefreshTime);
        templateMap.put("afterRefreshTime", afterRefreshTime);

        EmailData emailData = EmailData
                .builder()
                .emailSubject(EMAIL_SUBJECT)
                .templateMap(templateMap)
                .build();
        emailService.sendEmail(emailData);
    }
}
