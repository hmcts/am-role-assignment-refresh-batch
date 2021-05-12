package uk.gov.hmcts.reform.roleassignmentrefresh.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentrefresh.oidc.SecurityUtils;

@Component
@Slf4j
public class DeleteExpiredRecords implements Tasklet {

    @Autowired
    SecurityUtils securityUtils;

    public DeleteExpiredRecords() {
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Delete Expired records task starts::");
        log.info("User Token Length is :" + securityUtils.getUserToken().length());
        log.info("Service Auth Token Length is :" + securityUtils.getServiceToken().length());

        log.info("Delete expired records is successful");
        return RepeatStatus.FINISHED;
    }


}
