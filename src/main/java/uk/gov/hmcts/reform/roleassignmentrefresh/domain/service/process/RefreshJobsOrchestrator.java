package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentrefresh.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.ORMFeignClient;

import java.util.List;
import java.util.Objects;

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
        }
        log.info(" >> Refresh Batch Job({}) execution finished at {} . Time taken = {} milliseconds",
                jobs.size(), System.currentTimeMillis(), Math.subtractExact(System.currentTimeMillis(), startTime)
        );
    }

    private void sendJobToORMService(Long jobId, UserRequest userRequest) {
        ResponseEntity<Object> responseEntity =  ormFeignClient.sendJobToRoleAssignmentBatchService(jobId, userRequest);
        if (responseEntity.getStatusCode() != HttpStatus.ACCEPTED) {
            throw new UnprocessableEntityException(responseEntity.toString());
        }
    }
}

