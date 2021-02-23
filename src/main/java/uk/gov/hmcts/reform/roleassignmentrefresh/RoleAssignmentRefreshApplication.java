package uk.gov.hmcts.reform.roleassignmentrefresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SuppressWarnings("HideUtilityClassConstructor")
public class RoleAssignmentRefreshApplication {

    private static final Logger log = LoggerFactory.getLogger(RoleAssignmentRefreshApplication.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(RoleAssignmentRefreshApplication.class, args);
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(1000 * 6);
        int exitCode = SpringApplication.exit(context);
        String exitCodeLog = String.format("RoleAssignmentRefreshApplication Application exiting with exit code %s",
                exitCode);
        log.info(exitCodeLog);
        System.exit(exitCode);
    }
}
