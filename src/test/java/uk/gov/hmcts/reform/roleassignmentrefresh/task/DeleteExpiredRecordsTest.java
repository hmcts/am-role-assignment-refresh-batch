package uk.gov.hmcts.reform.roleassignmentrefresh.task;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class DeleteExpiredRecordsTest {

    @Mock
    StepContribution stepContribution = Mockito.mock(StepContribution.class);

    @Mock
    ChunkContext chunkContext = Mockito.mock(ChunkContext.class);

    private final DeleteExpiredRecords sut = new DeleteExpiredRecords();

    @BeforeAll
    public static void setUp() {
        //MockitoAnnotations.initMocks(this);
    }

    @Test
    public void dummyTest() {
        int testInt = 5;
        assertEquals(6, testInt + 1);
    }


}
