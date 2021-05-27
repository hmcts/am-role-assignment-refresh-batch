package uk.gov.hmcts.reform.roleassignmentbatch.launchdarkly;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import uk.gov.hmcts.reform.roleassignmentrefresh.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process.RefreshJobsOrchestrator;
import uk.gov.hmcts.reform.roleassignmentrefresh.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.roleassignmentrefresh.task.RefreshORMRules;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeatureConditionEvaluatorTest {

    @Mock
    private RefreshJobsOrchestrator refreshJobsOrchestrator;

    @Mock
    LDClient ldClient = mock(LDClient.class);

    @Mock
    FeatureConditionEvaluator featureConditionEvaluator = mock(FeatureConditionEvaluator.class);

    @InjectMocks
    RefreshORMRules refreshORMRules;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void verifyBatchJobFromTaskTest() {
        when(featureConditionEvaluator.isFlagEnabled(any(), any())).thenReturn(true);
        RepeatStatus status = refreshORMRules.execute(mock(StepContribution.class), mock(ChunkContext.class));
        assertEquals(status, RepeatStatus.FINISHED);
    }

    @Test(expected = ForbiddenException.class)
    public void getExceptionForFlagFromJob() {
        refreshORMRules.execute(mock(StepContribution.class), mock(ChunkContext.class));
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
