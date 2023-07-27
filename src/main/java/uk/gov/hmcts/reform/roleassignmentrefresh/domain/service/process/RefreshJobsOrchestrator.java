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
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.SendJobDetailsService;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class RefreshJobsOrchestrator {

    private final PersistenceService persistenceService;
    private final SendJobDetailsService jobDetailsService;

    @Value("${refresh-job-delay-enabled}")
    private boolean refreshJobDelayEnabled;

    @Value("${refresh-job-delay-duration}")
    private int refreshJobDelayDuration;

    @Autowired
    public RefreshJobsOrchestrator(PersistenceService persistenceService,
                                   SendJobDetailsService jobDetailsService) {
        this.persistenceService = persistenceService;
        this.jobDetailsService = jobDetailsService;

    }

    public void processRefreshJobs() {
        long startTime = System.currentTimeMillis();
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
            if (refreshJobDelayEnabled) {
                try {
                    Thread.sleep(refreshJobDelayDuration);
                } catch (InterruptedException e) {
                    log.error("Role Assignment Refresh Batch delay was interrupted");
                }
            }
        }
        log.info(" >> Refresh Batch Job({}) execution finished at {} . Time taken = {} milliseconds",
                jobs.size(), System.currentTimeMillis(), Math.subtractExact(System.currentTimeMillis(), startTime)
        );
    }

    private void sendJobToORMService(Long jobId, UserRequest userRequest) {
        ResponseEntity<Object> responseEntity =  jobDetailsService.sendToRoleAssignmentBatchService(jobId, userRequest);
        if (responseEntity.getStatusCode() != HttpStatus.ACCEPTED) {
            throw new UnprocessableEntityException(responseEntity.toString());
        }
    }
}

