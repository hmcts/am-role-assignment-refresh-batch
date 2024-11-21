package uk.gov.hmcts.reform.roleassignmentrefresh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process.RefreshJobsOrchestrator;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;


@Component
public class RefreshORMRules implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(RefreshORMRules.class);

    @Autowired
    private RefreshJobsOrchestrator refreshJobsOrchestrator;

    @Override
    @WithSpan(value = "Refresh ORM Rules ", kind = SpanKind.SERVER)
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.debug("Refresh Job task starts::");
        refreshJobsOrchestrator.processRefreshJobs();
        log.debug("Refresh Job is successful");
        return RepeatStatus.FINISHED;
    }

}
