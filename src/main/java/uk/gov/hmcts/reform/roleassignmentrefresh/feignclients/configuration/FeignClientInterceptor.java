package uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentrefresh.oidc.SecurityUtils;

import static uk.gov.hmcts.reform.roleassignmentrefresh.constants.RefreshConstants.BEARER;

@Service
@Slf4j
public class FeignClientInterceptor {

    @Autowired
    SecurityUtils securityUtils;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            log.debug("The Request template URL is {}",requestTemplate.url());
            if (!requestTemplate.url().equals("/lease") && !requestTemplate.url().equals("/o/token")) {
                requestTemplate.header("ServiceAuthorization", BEARER + securityUtils.getServiceToken());
                requestTemplate.header(HttpHeaders.AUTHORIZATION, BEARER + securityUtils.getUserToken());
                requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");
            }
        };
    }


}
