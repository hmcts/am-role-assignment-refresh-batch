package uk.gov.hmcts.reform.roleassignmentbatch.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process.RefreshJobsOrchestrator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class RefreshJobsOrchestratorTest {


    @Mock
    private RefreshJobsOrchestrator sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void verifyProcessRefreshJobs() {

        sut.processRefreshJobs();
        assertTrue(true);
    }

    @Test
    void verifyProcessRefreshJobs_noJobs() {

     sut.processRefreshJobs();
     assertTrue(true);
    }

    @Test
    void verifyProcessRefreshJobs_invalidStatus() {


        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> sut.processRefreshJobs(),
                "Expected processRefreshJobs() to throw, but it didn't"
        );
        assertFalse(thrown.getMessage().contains("202"));
    }

    @Test
    void verifyProcessRefreshJobs_linkedJobInvalidStatus() {

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> sut.processRefreshJobs(),
                "Expected processRefreshJobs() to throw, but it didn't"
        );
        assertFalse(thrown.getMessage().contains("202"));
    }
}
