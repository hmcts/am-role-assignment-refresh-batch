package uk.gov.hmcts.reform.roleassignmentbatch.task;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RefreshORMRulesTest {
    @Mock
    private RefreshJobsOrchestrator refreshJobsOrchestrator;

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
        assertEquals(RepeatStatus.FINISHED, status);
    }

    @Test(expected = ForbiddenException.class)
    public void getExceptionForFlagFromJob() {
        refreshORMRules.execute(mock(StepContribution.class), mock(ChunkContext.class));
    }
}
