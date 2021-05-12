package uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration;

import com.launchdarkly.shaded.okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfiguration {

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }

}
