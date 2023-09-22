package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.ORMFeignClient;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.RASFeignClient;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.SendJobDetailsService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.SendGetUserCountService;
import uk.gov.hmcts.reform.roleassignmentrefresh.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobRepository;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.Status;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshJobsOrchestratorTest {

    @Mock
    private final RefreshJobRepository refreshJobRepository = mock(RefreshJobRepository.class);

    private final ORMFeignClient ormFeignClient = mock(ORMFeignClient.class);
    private final RASFeignClient rasFeignClient = mock(RASFeignClient.class);

    @InjectMocks
    private final PersistenceService persistenceService = new PersistenceService();

    @InjectMocks
    private final SendJobDetailsService sendJobDetailsService = new SendJobDetailsService(ormFeignClient);

    @InjectMocks
    private final SendGetUserCountService sendGetUserCountService = new SendGetUserCountService(rasFeignClient);

    @InjectMocks
    private final RefreshJobsOrchestrator sut = new RefreshJobsOrchestrator(persistenceService, sendJobDetailsService,
            sendGetUserCountService);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void verifyProcessRefreshJobs() {

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()),
                TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());
        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);
        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        sut.processRefreshJobs();
        assertTrue(true);

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobsWithDelay() {

        ReflectionTestUtils.setField(sut, "refreshJobDelayDuration", 500);

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()),
                TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());
        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);
        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        sut.processRefreshJobs();

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyInterruptExceptionHandledDuringProcessRefreshJobsWithDelayIfInterrupted() {

        ReflectionTestUtils.setField(sut, "refreshJobDelayDuration", 500);

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()),
                TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());
        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);
        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        // ensure that delay is interrupted
        Thread.currentThread().interrupt();

        sut.processRefreshJobs();

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobs_noJobs() {

        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(Collections.emptyList());

        sut.processRefreshJobs();
        assertTrue(true);

        verify(ormFeignClient, times(0)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobs_oddJobs() {
        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()),
                TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());
        jobEntities.get(0).setLinkedJobId(0L);

        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);
        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));
        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        sut.processRefreshJobs();
        assertTrue(true);

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobs_invalidStatus() {

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()),
                TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());
        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);

        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        RuntimeException thrown = assertThrows(RuntimeException.class, sut::processRefreshJobs,
                "Expected processRefreshJobs() to throw, but it didn't"
        );
        // assertFalse(thrown.getMessage().contains("202"))

        verify(ormFeignClient, times(1)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobs_linkedJobInvalidStatus() {

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());

        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);

        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        RuntimeException thrown = assertThrows(RuntimeException.class, sut::processRefreshJobs,
                "Expected processRefreshJobs() to throw, but it didn't"
        );
        assertFalse(thrown.getMessage().contains("202"));

        verify(ormFeignClient, times(1)).sendJobToRoleAssignmentBatchService(any(), any());
    }
}

