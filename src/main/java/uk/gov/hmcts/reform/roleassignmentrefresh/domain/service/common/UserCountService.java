package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class UserCountService {
    private final RASFeignClient rasFeignClient;

    public UserCountService(RASFeignClient rasFeignClient) {
        this.rasFeignClient = rasFeignClient;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public ResponseEntity<Object> getRasUserCounts() {
        return rasFeignClient.getUserCounts();
    }
}
