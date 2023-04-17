package uk.gov.hmcts.reform.roleassignmentrefresh;

import com.microsoft.applicationinsights.TelemetryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@SpringBootApplication
@EnableFeignClients(basePackages = { "uk.gov.hmcts.reform.roleassignmentrefresh"},
        basePackageClasses = {IdamApi.class, ServiceAuthorisationApi.class})
@SuppressWarnings("HideUtilityClassConstructor")
public class RoleAssignmentRefreshApplication {

    private static final Logger log = LoggerFactory.getLogger(RoleAssignmentRefreshApplication.class);

    @Autowired
    private static TelemetryClient client;

    @Autowired
    public RoleAssignmentRefreshApplication(TelemetryClient client) {
        RoleAssignmentRefreshApplication.client = client;
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(RoleAssignmentRefreshApplication.class, args);

        int exitCode = SpringApplication.exit(context);
        String exitCodeLog = String.format("RoleAssignmentRefreshApplication Application exiting with exit code %s",
                exitCode);
        log.info(exitCodeLog);
        client.flush();
        //Sleep added to allow app-insights to flush the logs
        Thread.sleep(1000 * 8);
        System.exit(exitCode);
    }
}
