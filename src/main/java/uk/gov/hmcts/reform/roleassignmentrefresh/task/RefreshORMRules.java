package uk.gov.hmcts.reform.roleassignmentrefresh.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignmentrefresh.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process.RefreshJobsOrchestrator;
import uk.gov.hmcts.reform.roleassignmentrefresh.launchdarkly.FeatureConditionEvaluator;

@Component
public class RefreshORMRules implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(RefreshORMRules.class);

    private static final String SERVICE_NAME = "am_role_assignment_refresh_batch";

    @Autowired
    private RefreshJobsOrchestrator refreshJobsOrchestrator;

    @Autowired
    private FeatureConditionEvaluator featureConditionEvaluator;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("Sys outing the details");
        log.info("ORM_IDAM_CLIENT_ID: " + System.getenv("ORM_IDAM_CLIENT_ID"));
        log.info("ORG_ROLE_MAPPING_IDAM_CLIENT_SECRET: " + System.getenv("ORG_ROLE_MAPPING_IDAM_CLIENT_SECRET"));
        log.info("IDAM_ADMIN_PASSWORD: " + System.getenv("ORG_ROLE_MAPPING_IDAM_ADMIN_PASSWORD"));
        log.info("BATCH_SECRET: " + System.getenv("AM_ROLE_ASSIGNMENT_REFRESH_BATCH_SECRET"));
        log.info("LD_SDK_KEY: " + System.getenv("LD_SDK_KEY"));
        log.info("ORG_ROLE_MAPPING_DB_PASSWORD: " + System.getenv("ORG_ROLE_MAPPING_DB_PASSWORD"));
        log.info("Sys outing the details : end");

        if (featureConditionEvaluator.isFlagEnabled(SERVICE_NAME, "orm-refresh-role")) {
            log.debug("Refresh Job task starts::");
            refreshJobsOrchestrator.processRefreshJobs();
            log.debug("Refresh Job is successful");
            return RepeatStatus.FINISHED;
        } else {
            throw new ForbiddenException(String.format("Launch Darkly flag is not enabled for the batch job %s",
                    SERVICE_NAME));
        }
    }

}
