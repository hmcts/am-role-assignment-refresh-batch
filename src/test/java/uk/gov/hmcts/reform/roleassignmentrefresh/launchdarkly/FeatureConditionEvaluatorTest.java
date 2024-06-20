package uk.gov.hmcts.reform.roleassignmentrefresh.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureConditionEvaluatorTest {

    @Mock
    LDClient ldClient = mock(LDClient.class);

    @InjectMocks
    FeatureConditionEvaluator featureConditionEvaluator;

    @Test
    void getPositiveResponseForFlag() {
        when(ldClient.boolVariation(any(), any(), anyBoolean())).thenReturn(true);
        featureConditionEvaluator = new FeatureConditionEvaluator(ldClient, "", "");
        assertTrue(featureConditionEvaluator.isFlagEnabled("am_role_assignment_refresh_batch",
                "orm-refresh-role"));
    }

    @Test
    void getNegativeResponseForFlag() {
        assertFalse(featureConditionEvaluator.isFlagEnabled("am_role_assignment_refresh_batch",
                ""));
    }

}
