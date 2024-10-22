package uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.CountResponse;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.RASFeignClient;

@Component
public class RASFeignClientFallback implements RASFeignClient {
    @Override
    public ResponseEntity<CountResponse> getUserCounts() {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
