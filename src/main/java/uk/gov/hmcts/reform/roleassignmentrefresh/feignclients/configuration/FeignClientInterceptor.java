package uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentrefresh.oidc.SecurityUtils;

import static uk.gov.hmcts.reform.roleassignmentrefresh.constants.RefreshConstants.BEARER;

@Service
public class FeignClientInterceptor {

    @Autowired
    SecurityUtils securityUtils;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("ServiceAuthorization", BEARER + securityUtils.getServiceToken());
            requestTemplate.header(HttpHeaders.AUTHORIZATION, BEARER + securityUtils.getUserToken());
            requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");
        };
    }


}
