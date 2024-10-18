package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.roleassignmentrefresh.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobRepository;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.Count;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.EmailData;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.EmailService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.ORMFeignClient;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.RASFeignClient;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.SendJobDetailsService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.UserCountService;
import uk.gov.hmcts.reform.roleassignmentrefresh.helper.TestDataBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    private final UserCountService userCountService = new UserCountService(rasFeignClient);

    private final EmailService emailService = mock(EmailService.class);

    @InjectMocks
    private final RefreshJobsOrchestrator sut = new RefreshJobsOrchestrator(persistenceService, sendJobDetailsService,
            userCountService, emailService);

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

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        sut.processRefreshJobs();
        assertTrue(true);

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobsWithDelay() {

        ReflectionTestUtils.setField(sut, "refreshJobDelayDuration", 500);
        ReflectionTestUtils.setField(sut, "refreshJobCountDelayDuration", 500);

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()),
                TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());
        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);
        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        sut.processRefreshJobs();

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
        // NB: must call count twice when jobs to process, i.e. before and after
        verify(rasFeignClient, times(2)).getUserCounts();
    }

    @Test
    void verifyInterruptExceptionHandledDuringProcessRefreshJobsWithDelayIfInterrupted() {

        ReflectionTestUtils.setField(sut, "refreshJobDelayDuration", 500);
        ReflectionTestUtils.setField(sut, "refreshJobCountDelayDuration", 500);

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()),
                TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());
        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);
        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // ensure that delay is interrupted
        Thread.currentThread().interrupt();

        sut.processRefreshJobs();

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobs_noExceptionWhenUserCountFails() {

        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(Collections.emptyList());

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        sut.processRefreshJobs();

        verify(ormFeignClient, times(0)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobs_noJobs() {

        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(Collections.emptyList());

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        sut.processRefreshJobs();
        assertTrue(true);

        verify(ormFeignClient, times(0)).sendJobToRoleAssignmentBatchService(any(), any());
        // NB: must call count once when no jobs
        verify(rasFeignClient, times(1)).getUserCounts();
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
        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        sut.processRefreshJobs();

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobs_invalidStatus() {

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()));

        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        RuntimeException thrown = assertThrows(UnprocessableEntityException.class, sut::processRefreshJobs,
                "Expected processRefreshJobs() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains(HttpStatus.BAD_REQUEST.getReasonPhrase()));

        verify(ormFeignClient, times(1)).sendJobToRoleAssignmentBatchService(any(), any());
        // NB: no call to refreshJobRepository.findById as no linked job
        verify(refreshJobRepository, never()).findById(any());
    }

    @Test
    void verifyProcessRefreshJobs_linkedJobInvalidStatus() {

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());

        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);

        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        RuntimeException thrown = assertThrows(UnprocessableEntityException.class, sut::processRefreshJobs,
                "Expected processRefreshJobs() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains(HttpStatus.BAD_REQUEST.getReasonPhrase()));

        verify(ormFeignClient, times(1)).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobs_linkedJobNotFound() {

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());

        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);

        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty()); // i.e. not found

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        sut.processRefreshJobs();

        // NB: job not triggered as invalid linked job
        verify(ormFeignClient, never()).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    void verifyProcessRefreshJobs_linkedJobNoUsers() {

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());

        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);

        RefreshJobEntity linkedJob = TestDataBuilder.buildRefreshJobEntity(Status.ABORTED.name());
        linkedJob.setUserIds(null); // ensure no userIds for test
        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(linkedJob));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        sut.processRefreshJobs();

        // NB: job not triggered as invalid linked job
        verify(ormFeignClient, never()).sendJobToRoleAssignmentBatchService(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void verifyProcessRefreshJobsWithCountEmail() throws IOException {

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()),
                TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());
        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);
        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));


        String beforeFilePath = "src/test/resources/countResponseBefore.json";
        String beforeResponseString = new String(Files.readAllBytes(Paths.get(beforeFilePath)));

        String afterFilePath = "src/test/resources/countResponseAfter.json";
        String afterResponseString = new String(Files.readAllBytes(Paths.get(afterFilePath)));

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(beforeResponseString, HttpStatus.OK))
                .thenReturn(new ResponseEntity<>(afterResponseString, HttpStatus.OK));

        sut.processRefreshJobs();
        assertTrue(true);

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
        verify(rasFeignClient, times(2)).getUserCounts();

        ArgumentCaptor<EmailData> emailDataArgumentCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(emailService, times(1)).sendEmail(emailDataArgumentCaptor.capture());
        Map<String,Object> templateMap = emailDataArgumentCaptor.getValue().getTemplateMap();

        List<Count> jurisdictionCounts = (List<Count>) templateMap.get("jurisdictionCount");

        // assertions for jurisdictionCounts
        assertEquals(4, jurisdictionCounts.size());

        assertCount(jurisdictionCounts.get(0), null, "LEGAL_OPERATIONS", null, 3, 2, -1);
        assertCount(jurisdictionCounts.get(1), "CIVIL", "LEGAL_OPERATIONS", null, 0, 2, 2);
        assertCount(jurisdictionCounts.get(2), "IA", "JUDICIAL", null, 7, 8, 1);
        assertCount(jurisdictionCounts.get(3), "IA", "LEGAL_OPERATIONS", null, 3, 0, -3);

        List<Count> jurisdictionAndRoleNameCounts = (List<Count>) templateMap.get("jurisdictionAndRoleNameCount");

        // assertions for jurisdictionAndRoleNameCounts
        assertEquals(9, jurisdictionAndRoleNameCounts.size());

        assertCount(jurisdictionAndRoleNameCounts.get(0), null, "LEGAL_OPERATIONS", "hmcts-legal-operations", 3, 4, 1);
        assertCount(jurisdictionAndRoleNameCounts.get(1), "CIVIL", "LEGAL_OPERATIONS", "hmcts-legal-operations",
                0, 4, 4);
        assertCount(jurisdictionAndRoleNameCounts.get(2), "IA", "JUDICIAL", "judge", 7, 8, 1);
        assertCount(jurisdictionAndRoleNameCounts.get(3), "IA", "LEGAL_OPERATIONS", "case-allocator", 3, 0, -3);
        assertCount(jurisdictionAndRoleNameCounts.get(4), "IA", "LEGAL_OPERATIONS", "hearing-manager", 3, 2, -1);
        assertCount(jurisdictionAndRoleNameCounts.get(5), "IA", "LEGAL_OPERATIONS", "hearing-viewer", 3, 3, 0);
        assertCount(jurisdictionAndRoleNameCounts.get(6), "IA", "LEGAL_OPERATIONS", "senior-tribunal-caseworker",
                3, 3, 0);
        assertCount(jurisdictionAndRoleNameCounts.get(7), "IA", "LEGAL_OPERATIONS", "task-supervisor", 3, 3, 0);
        assertCount(jurisdictionAndRoleNameCounts.get(8), "IA", "LEGAL_OPERATIONS", "tribunal-caseworker", 3, 3, 0);
    }

    @Test
    void verifyProcessRefreshJobsWithCountEmailBeforeCountResponseBrackets() throws IOException {

        List<RefreshJobEntity> jobEntities = List.of(TestDataBuilder.buildRefreshJobEntity(Status.NEW.name()),
                TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities());
        when(refreshJobRepository.findByStatusOrderByCreatedDesc(any(String.class)))
                .thenReturn(jobEntities);
        when(refreshJobRepository.findById(any(Long.class)))
                .thenReturn(TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name()));

        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.ACCEPTED));

        String beforeResponseString = "{}";

        String afterFilePath = "src/test/resources/countResponseAfter.json";
        String afterResponseString = new String(Files.readAllBytes(Paths.get(afterFilePath)));

        when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(beforeResponseString, HttpStatus.OK))
                .thenReturn(new ResponseEntity<>(afterResponseString, HttpStatus.OK));

        sut.processRefreshJobs();
        assertTrue(true);

        verify(ormFeignClient, times(2)).sendJobToRoleAssignmentBatchService(any(), any());
        verify(rasFeignClient, times(2)).getUserCounts();
    }

    private void assertCount(Count count, String expectedJurisdiction, String expectedRoleCategory,
                             String expectedRoleName, int expectedBeforeCount, int expectedAfterCount,
                             int expectedDifference) {
        assertEquals(expectedJurisdiction, count.getJurisdiction());
        assertEquals(expectedRoleCategory, count.getRoleCategory());
        assertEquals(expectedRoleName, count.getRoleName());
        assertEquals(expectedBeforeCount, count.getBeforeCount());
        assertEquals(expectedAfterCount, count.getAfterCount());
        assertEquals(expectedDifference, count.getDifference());
    }
}
