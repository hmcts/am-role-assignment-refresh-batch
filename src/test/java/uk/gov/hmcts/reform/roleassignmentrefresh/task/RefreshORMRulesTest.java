package uk.gov.hmcts.reform.roleassignmentrefresh.task;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process.RefreshJobsOrchestrator;
import uk.gov.hmcts.reform.roleassignmentrefresh.launchdarkly.FeatureConditionEvaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class RefreshORMRulesTest {
    @Mock
    private RefreshJobsOrchestrator refreshJobsOrchestrator;

    @Mock
    FeatureConditionEvaluator featureConditionEvaluator = mock(FeatureConditionEvaluator.class);

    @InjectMocks
    RefreshORMRules refreshORMRules;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void verifyBatchJobFromTaskTest() {
        when(featureConditionEvaluator.isFlagEnabled(any(), any())).thenReturn(true);
        RepeatStatus status = refreshORMRules.execute(mock(StepContribution.class), mock(ChunkContext.class));
        assertEquals(RepeatStatus.FINISHED, status);
    }

    @Test
    void shouldFinishTaskWhenLDIsDisabled() {
        when(featureConditionEvaluator.isFlagEnabled(any(), any())).thenReturn(false);
        RepeatStatus status = refreshORMRules.execute(mock(StepContribution.class), mock(ChunkContext.class));
        assertEquals(RepeatStatus.FINISHED, status);
        verify(refreshJobsOrchestrator, times(1)).sendGetUserCountToRASService();
    }
}
