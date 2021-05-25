package uk.gov.hmcts.reform.roleassignmentrefresh.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class FeatureConditionEvaluator {

    public static final String USER = "user";
    public static final String SERVICE_NAME = "servicename";

    @Autowired
    private LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Value("${launchdarkly.sdk.user}")
    private String userName;

    public boolean isFlagEnabled(String serviceName, String flagName) {
        LDUser user = new LDUser.Builder(environment)
                .firstName(userName)
                .lastName(USER)
                .custom(SERVICE_NAME, serviceName)
                .build();
        log.info("Env:" + environment);

        log.info("userName:" + userName);
        log.info("Env:" + environment);
        log.info("user:" + user);
        log.info("serviceName:" + serviceName);
        log.info("flagName:" + flagName);
        log.info("Flag value: " + ldClient.boolVariation(flagName, user, false));
        log.info("LDClient: " + ldClient.toString());
        return ldClient.boolVariation(flagName, user, false);
    }

}
