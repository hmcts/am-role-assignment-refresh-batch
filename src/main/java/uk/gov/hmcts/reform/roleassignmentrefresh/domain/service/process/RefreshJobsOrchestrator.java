package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.ORMFeignClient;

import java.util.List;

@Slf4j
@Service
public class RefreshJobsOrchestrator {

    private final PersistenceService persistenceService;
    private final ORMFeignClient ormFeignClient;

    @Autowired
    public RefreshJobsOrchestrator(PersistenceService persistenceService,
                                   ORMFeignClient ormFeignClient) {
        this.persistenceService = persistenceService;
        this.ormFeignClient = ormFeignClient;

    }

    public void processRefreshJobs() {
        long startTime = System.currentTimeMillis();
        // Get new job entries for refresh
        List<RefreshJobEntity> newJobs =  persistenceService.getNewJobs();
        for (RefreshJobEntity job: newJobs) {
            ResponseEntity<Object> responseEntity =  ormFeignClient.sendJobToRoleAssignmentBatchService(job.getJobId(),
                    UserRequest.builder().build());
            if (responseEntity.getStatusCode() != HttpStatus.ACCEPTED) {
                throw new RuntimeException(responseEntity.toString());
            }
        }

        //get failed job entries to process linked users
        List<RefreshJobEntity> failedJobs =  persistenceService.getNewJobsWithLinkedJob();
        for (RefreshJobEntity job: failedJobs) {
            RefreshJobEntity linkedJob = persistenceService.getByJobId(job.getLinkedJobId());
            if (linkedJob != null && ArrayUtils.isNotEmpty(linkedJob.getUserIds())) {
                ResponseEntity<Object> responseEntity =  ormFeignClient.sendJobToRoleAssignmentBatchService(
                        job.getJobId(), UserRequest.builder().userIds(linkedJob.getUserIds()).build());
                if (responseEntity.getStatusCode() != HttpStatus.ACCEPTED) {
                    throw new RuntimeException(responseEntity.toString());
                }
            }
        }
        log.info(" >> Refresh Batch Job execution finished at {} . Time taken = {} milliseconds",
                System.currentTimeMillis(), Math.subtractExact(System.currentTimeMillis(), startTime)
        );
    }
}

