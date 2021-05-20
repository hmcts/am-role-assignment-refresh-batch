package uk.gov.hmcts.reform.roleassignmentrefresh.launchdarkly;


import com.launchdarkly.sdk.server.LDClient;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class FeatureConditionEvaluator {

    @Autowired
    private LDClient ldClient;


    public boolean isFlagEnabled(String serviceName, String flagName) {


        return ldClient.boolVariation(flagName, null, false);
    }

}
