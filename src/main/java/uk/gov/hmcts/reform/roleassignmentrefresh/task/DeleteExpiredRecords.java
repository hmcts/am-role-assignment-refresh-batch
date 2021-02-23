package uk.gov.hmcts.reform.roleassignmentrefresh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class DeleteExpiredRecords implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(DeleteExpiredRecords.class);

    public DeleteExpiredRecords() {
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Delete Expired records task starts::");


        log.info("Delete expired records is successful");
        return RepeatStatus.FINISHED;
    }




}
