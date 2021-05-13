package uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class FeignClientInterceptor {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (!requestTemplate.url().contains("health")) {
                requestTemplate.header("ServiceAuthorization", "Bearer ");
                requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer ");
                requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");
            }
        };
    }


}
