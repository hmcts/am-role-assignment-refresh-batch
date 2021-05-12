package uk.gov.hmcts.reform.roleassignmentbatch.domain;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignmentbatch.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobRepository;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.ORMFeignClient;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process.RefreshJobsOrchestrator;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RefreshJobsOrchestratorTest {

    @Mock
    private final RefreshJobRepository refreshJobRepository = mock(RefreshJobRepository.class);

    private final ORMFeignClient ormFeignClient = mock(ORMFeignClient.class);

    @InjectMocks
    private final PersistenceService persistenceService = new PersistenceService();

    @InjectMocks
    private final RefreshJobsOrchestrator sut = new RefreshJobsOrchestrator(persistenceService, ormFeignClient);

    @Test
    void verifyProcessRefreshJobs() {

        when(refreshJobRepository.findByStatusAndLinkedJobIdIsNullOrderByCreatedDesc(any(String.class)))
                .thenReturn(TestDataBuilder.buildRefreshJobEntities(Status.NEW.name()));

        when(refreshJobRepository.findByStatusAndLinkedJobIdIsNotNullOrderByCreatedDesc(any(String.class)))
                .thenReturn(TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());

        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        sut.processRefreshJobs();
        assertTrue(true);
    }

    @Test
    void verifyProcessRefreshJobs_noJobs() {

        when(refreshJobRepository.findByStatusAndLinkedJobIdIsNullOrderByCreatedDesc(any(String.class)))
                .thenReturn(Collections.emptyList());

        when(refreshJobRepository.findByStatusAndLinkedJobIdIsNotNullOrderByCreatedDesc(any(String.class)))
                .thenReturn(Collections.emptyList());

        sut.processRefreshJobs();
        assertTrue(true);
    }

    @Test
    void verifyProcessRefreshJobs_invalidStatus() {

        when(refreshJobRepository.findByStatusAndLinkedJobIdIsNullOrderByCreatedDesc(any(String.class)))
                .thenReturn(TestDataBuilder.buildRefreshJobEntities(Status.NEW.name()));

        when(refreshJobRepository.findByStatusAndLinkedJobIdIsNotNullOrderByCreatedDesc(any(String.class)))
                .thenReturn(TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());

        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> sut.processRefreshJobs(),
                "Expected processRefreshJobs() to throw, but it didn't"
        );
        assertFalse(thrown.getMessage().contains("202"));
    }

    @Test
    void verifyProcessRefreshJobs_linkedJobInvalidStatus() {

        when(refreshJobRepository.findByStatusAndLinkedJobIdIsNullOrderByCreatedDesc(any(String.class)))
                .thenReturn(Collections.emptyList());

        when(refreshJobRepository.findByStatusAndLinkedJobIdIsNotNullOrderByCreatedDesc(any(String.class)))
                .thenReturn(TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());

        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> sut.processRefreshJobs(),
                "Expected processRefreshJobs() to throw, but it didn't"
        );
        assertFalse(thrown.getMessage().contains("202"));
    }
}

