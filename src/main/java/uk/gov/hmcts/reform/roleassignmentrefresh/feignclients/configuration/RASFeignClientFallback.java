package uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.RASFeignClient;

@Component
public class RASFeignClientFallback implements RASFeignClient {

    public static final String RAS_API_NOT_AVAILABLE = "The RAS API Service is not available";

    @Override
    public ResponseEntity<Object> sendGetUserCountToRoleAssignmentService() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(RAS_API_NOT_AVAILABLE);
    }
}
