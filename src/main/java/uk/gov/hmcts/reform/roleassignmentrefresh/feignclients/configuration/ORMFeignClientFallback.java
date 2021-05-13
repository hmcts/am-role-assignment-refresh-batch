package uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.ORMFeignClient;

@Component
public class ORMFeignClientFallback implements ORMFeignClient {

    public static final String ORM_API_NOT_AVAILABLE = "The ORM API Service is not available";

    @Override
    public ResponseEntity<Object> sendJobToRoleAssignmentBatchService(Long jobId, UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ORM_API_NOT_AVAILABLE);
    }

}
