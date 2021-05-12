package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;
import uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration.FeignClientInterceptor;
import uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration.ORMFeignClientFallback;
import uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration.FeignClientConfiguration;

@FeignClient(value = "orgrolemappingclient", url = "${feign.client.config.ormClient.url}",
        configuration = {FeignClientConfiguration.class, FeignClientInterceptor.class},
        fallback = ORMFeignClientFallback.class)
public interface ORMFeignClient {

    @PostMapping(value = "/am/role-mapping/refresh")
    ResponseEntity<Object> sendJobToRoleAssignmentBatchService(@RequestParam(value = "jobId") Long jobId,
                                                               @RequestBody UserRequest userRequest);
}
