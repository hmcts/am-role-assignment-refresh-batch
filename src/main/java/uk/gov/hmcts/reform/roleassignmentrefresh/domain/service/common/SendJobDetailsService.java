package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;

@Service
public class SendJobDetailsService {
    private final ORMFeignClient ORMFeignClient;

    public SendJobDetailsService(ORMFeignClient ORMFeignClient) {
        this.ORMFeignClient = ORMFeignClient;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public ResponseEntity<Object> sendToRoleAssignmentBatchService(Long jobId, UserRequest userRequest) {
        return ORMFeignClient.sendJobToRoleAssignmentBatchService(jobId, userRequest);
    }
}
