package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobRepository;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.Status;

import java.util.List;

@Service
public class PersistenceService {

    @Autowired
    private RefreshJobRepository refreshJobRepository;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public List<RefreshJobEntity> getNewJobs() {
        return refreshJobRepository.findByStatusAndLinkedJobIdIsNullOrderByCreatedDesc(Status.NEW.name());
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public List<RefreshJobEntity> getNewJobsWithLinkedJob() {
        return refreshJobRepository.findByStatusAndLinkedJobIdIsNotNullOrderByCreatedDesc(Status.NEW.name());
    }

    public RefreshJobEntity getByJobId(Long jobId) {
        return refreshJobRepository.findById(jobId).orElse(null);
    }
}
