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
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.UserCountService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.SendJobDetailsService;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class RefreshJobsOrchestrator {

    private final PersistenceService persistenceService;
    private final SendJobDetailsService jobDetailsService;
    private final UserCountService userCountService;

    @Value("${refresh-job-delay-duration}")
    private long refreshJobDelayDuration;

    @Autowired
    public RefreshJobsOrchestrator(PersistenceService persistenceService,
                                   SendJobDetailsService jobDetailsService,
                                   UserCountService userCountService) {
        this.persistenceService = persistenceService;
        this.jobDetailsService = jobDetailsService;
        this.userCountService = userCountService;

    }

    public void processRefreshJobs() {
        final long startTime = System.currentTimeMillis();
        log.info("Calling RAS User Count Before Refresh");
        triggerRASUserCount();

        // Get new job entries for refresh
        List<RefreshJobEntity> jobs =  persistenceService.getNewJobs();
        for (RefreshJobEntity job: jobs) {
            if (Objects.isNull(job.getLinkedJobId()) ||  job.getLinkedJobId().equals(0L)) {
                sendJobToORMService(job.getJobId(), UserRequest.builder().build());
            } else {
                RefreshJobEntity linkedJob = persistenceService.getByJobId(job.getLinkedJobId());
                if (Objects.nonNull(linkedJob) && ArrayUtils.isNotEmpty(linkedJob.getUserIds())) {
                    sendJobToORMService(job.getJobId(), UserRequest.builder().userIds(linkedJob.getUserIds()).build());
                }
            }
            if (refreshJobDelayDuration > 0) {
                refreshJobDelay(refreshJobDelayDuration);
            }
        }

        log.info("Calling RAS User Count After Refresh");
        triggerRASUserCount();

        log.info(" >> Refresh Batch Job({}) execution finished at {} . Time taken = {} milliseconds",
                jobs.size(), System.currentTimeMillis(), Math.subtractExact(System.currentTimeMillis(), startTime)
        );
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

    public void triggerRASUserCount() {
        ResponseEntity<Object> responseEntity =  userCountService.getRasUserCounts();
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            log.error("Error return from RAS User Count: " + responseEntity.toString());
        }
    }
}

