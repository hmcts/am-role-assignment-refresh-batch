package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.advice.exception.UnprocessableEntityException;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobRepository;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.ORMFeignClient;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.RASFeignClient;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.SendJobDetailsService;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.UserCountService;
import uk.gov.hmcts.reform.roleassignmentrefresh.helper.TestDataBuilder;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(
    classes = {
        PersistenceService.class,
        RefreshJobsOrchestrator.class,
        SendJobDetailsService.class,
        UserCountService.class
    },
    properties = {
        "refresh-job-delay-duration=500",
        "refresh-job-count-delay-duration=500"
    }
)
public class RefreshJobsOrchestratorIntegrationTest {

    @Mock
    RASFeignClient rasFeignClient;

    @Mock
    ORMFeignClient ormFeignClient;

    @Mock
    RefreshJobRepository refreshJobRepository;

    @InjectMocks
    PersistenceService persistenceService;

    @InjectMocks
    SendJobDetailsService sendJobDetailsService;

    @InjectMocks
    UserCountService userCountService;

    RefreshJobsOrchestrator sut;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sut = new RefreshJobsOrchestrator(persistenceService, sendJobDetailsService, userCountService);
    }

    @Test
    public void triggerRASUserCount_shouldCallRasUserCount() {

        // GIVEN
        whenRsaUserCountCalledReturnStatus(HttpStatus.OK);

        // WHEN
        sut.triggerRASUserCount();

        // THEN
        verify(rasFeignClient, times(1)).getUserCounts();

    }

    @Test
    public void triggerRASUserCount_shouldSwallowRasUserCountFailure() {

        // GIVEN
        whenRsaUserCountCalledReturnStatus(HttpStatus.NOT_FOUND);

        // WHEN
        sut.triggerRASUserCount();

        // THEN
        verify(rasFeignClient, times(1)).getUserCounts();

    }

    @Test
    public void processRefreshJobs_noJobs_shouldStillTriggerRasUserCount() {

        // GIVEN
        whenRsaUserCountCalledReturnStatus(HttpStatus.OK);

        whenFindNewJobsCalledReturnList(List.of()); // i.e. empty list

        // WHEN
        sut.processRefreshJobs();

        // THEN
        verify(rasFeignClient, times(1)).getUserCounts();
        verifyNoInteractions(ormFeignClient); // i.e. no jobs triggered
    }

    @Test
    public void processRefreshJobs_singleJob_noLinkedJobId() {

        // GIVEN
        whenRsaUserCountCalledReturnStatus(HttpStatus.OK);

        RefreshJobEntity job = TestDataBuilder.buildRefreshJobEntity(Status.NEW.name());
        whenFindNewJobsCalledReturnList(List.of(job));

        whenOrmRefreshJobTriggerCalledReturnStatus(HttpStatus.ACCEPTED);

        // WHEN
        sut.processRefreshJobs();

        // THEN
        verify(rasFeignClient, times(2)).getUserCounts(); // before and after

        ArgumentCaptor<UserRequest> userRequestCaptor = ArgumentCaptor.forClass(UserRequest.class);
        verify(ormFeignClient, times(1)).sendJobToRoleAssignmentBatchService(
            eq(job.getJobId()),
            userRequestCaptor.capture()
        );
        assertTrue(ArrayUtils.isEmpty(userRequestCaptor.getValue().getUserIds())); // i.e. no users in submission
    }

    @Test
    public void processRefreshJobs_singleJob_withLinkedJobId() {

        // GIVEN
        whenRsaUserCountCalledReturnStatus(HttpStatus.OK);

        RefreshJobEntity job = TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities();
        whenFindNewJobsCalledReturnList(List.of(job));

        // set up linked job call
        Optional<RefreshJobEntity> linkedJob = TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name());
        when(refreshJobRepository.findById(job.getLinkedJobId())).thenReturn(linkedJob);
        // verify linked job has no userIds
        assert linkedJob.isPresent();
        String[] linkedUserIds = linkedJob.get().getUserIds();
        assertFalse(ArrayUtils.isEmpty(linkedUserIds));

        whenOrmRefreshJobTriggerCalledReturnStatus(HttpStatus.ACCEPTED);

        // WHEN
        sut.processRefreshJobs();

        // THEN
        verify(rasFeignClient, times(2)).getUserCounts(); // before and after

        ArgumentCaptor<UserRequest> userRequestCaptor = ArgumentCaptor.forClass(UserRequest.class);
        verify(ormFeignClient, times(1)).sendJobToRoleAssignmentBatchService(
            eq(job.getJobId()),
            userRequestCaptor.capture()
        );
        // verify linked user IDs have been submitted
        assertFalse(ArrayUtils.isEmpty(userRequestCaptor.getValue().getUserIds()));
        assertArrayEquals(linkedUserIds, userRequestCaptor.getValue().getUserIds());
    }

    @Test
    public void processRefreshJobs_singleJob_withLinkedJobId_misconfiguredLinkedJob() {

        // GIVEN
        whenRsaUserCountCalledReturnStatus(HttpStatus.OK);

        RefreshJobEntity job = TestDataBuilder.buildNewWithLinkedJobRefreshJobEntities();
        whenFindNewJobsCalledReturnList(List.of(job));

        // set up linked job call (misconfigred i.e. no userIds)
        Optional<RefreshJobEntity> linkedJob = TestDataBuilder.buildOptionalRefreshJobEntity(Status.ABORTED.name());
        // remove userIds (i.e to misconfigure the
        assert linkedJob.isPresent();
        linkedJob.get().setUserIds(null);
        when(refreshJobRepository.findById(job.getLinkedJobId())).thenReturn(linkedJob);

        whenOrmRefreshJobTriggerCalledReturnStatus(HttpStatus.ACCEPTED);

        // WHEN
        sut.processRefreshJobs();

        // THEN
        verifyNoInteractions(ormFeignClient); // i.e. no jobs triggered as linked User Ids missing
    }

    @Test
    public void processRefreshJobs_multipleJobs() {

        // GIVEN
        whenRsaUserCountCalledReturnStatus(HttpStatus.OK);

        RefreshJobEntity job1 = TestDataBuilder.buildRefreshJobEntity(Status.NEW.name());
        job1.setJobId(1L);
        RefreshJobEntity job2 = TestDataBuilder.buildRefreshJobEntity(Status.NEW.name());
        job2.setJobId(2L);
        RefreshJobEntity job3 = TestDataBuilder.buildRefreshJobEntity(Status.NEW.name());
        job3.setJobId(3L);
        whenFindNewJobsCalledReturnList(List.of(job1, job2, job3));

        whenOrmRefreshJobTriggerCalledReturnStatus(HttpStatus.ACCEPTED);

        // WHEN
        sut.processRefreshJobs();

        // THEN
        verify(rasFeignClient, times(2)).getUserCounts(); // before and after

        ArgumentCaptor<Long> jobIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(ormFeignClient, times(3)).sendJobToRoleAssignmentBatchService(
                jobIdCaptor.capture(),
                any()
        );
        assertTrue(jobIdCaptor.getAllValues().containsAll(List.of(1L, 2L, 3L)));
    }

    @Test
    public void processRefreshJobs_throwsUeeWhenOrmResponseUnrecognised() {

        // GIVEN
        whenRsaUserCountCalledReturnStatus(HttpStatus.OK);

        RefreshJobEntity job1 = TestDataBuilder.buildRefreshJobEntity(Status.NEW.name());
        whenFindNewJobsCalledReturnList(List.of(job1));

        whenOrmRefreshJobTriggerCalledReturnStatus(HttpStatus.BAD_REQUEST);

        // WHEN / THEN
        RuntimeException thrown = assertThrows(UnprocessableEntityException.class, sut::processRefreshJobs);
        assertTrue(thrown.getMessage().contains(HttpStatus.BAD_REQUEST.getReasonPhrase()));
    }

    private void whenFindNewJobsCalledReturnList(List<RefreshJobEntity> newJobs) {
        when(refreshJobRepository.findByStatusOrderByCreatedDesc(Status.NEW.name()))
                .thenReturn(newJobs);
    }

    private void whenOrmRefreshJobTriggerCalledReturnStatus(HttpStatus status) {
        when(ormFeignClient.sendJobToRoleAssignmentBatchService(any(), any()))
                .thenReturn(ResponseEntity.status(status).body(null));
    }

    private void whenRsaUserCountCalledReturnStatus(HttpStatus status) {
        when(rasFeignClient.getUserCounts())
                .thenReturn(ResponseEntity.status(status).body(null));
    }

}
