package uk.gov.hmcts.reform.roleassignmentrefresh.config;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LaunchDarklyConfiguration implements WebMvcConfigurer {

    @Value("${launchdarkly.runOnStartup:true}")
    private boolean runOnStartup;

    @Bean
    public LDClientInterface ldClient(@Value("${launchdarkly.sdk.key}") String sdkKey) {
        return runOnStartup ? new LDClient(sdkKey) : new LDDummyClient();
    }
}
