package uk.gov.hmcts.reform.roleassignmentrefresh.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeatureConditionEvaluatorTest {

    @Mock
    LDClient ldClient = mock(LDClient.class);

    @InjectMocks
    FeatureConditionEvaluator featureConditionEvaluator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getPositiveResponseForFlag() {
        when(ldClient.boolVariation(any(), any(), anyBoolean())).thenReturn(true);
        featureConditionEvaluator = new FeatureConditionEvaluator(ldClient, "", "");
        assertTrue(featureConditionEvaluator.isFlagEnabled("am_role_assignment_refresh_batch",
                "orm-refresh-role"));
    }

    @Test
    public void getNegativeResponseForFlag() {
        assertFalse(featureConditionEvaluator.isFlagEnabled("am_role_assignment_refresh_batch",
                ""));
    }

}
