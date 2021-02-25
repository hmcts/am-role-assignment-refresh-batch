package uk.gov.hmcts.reform.roleassignmentrefresh;

import feign.Feign;
import feign.jackson.JacksonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@EnableCaching
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SuppressWarnings("HideUtilityClassConstructor")
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.roleassignmentrefresh"}, basePackageClasses = {IdamApi.class,
        ServiceAuthorisationApi.class})
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

    @Bean
    public ServiceAuthorisationApi generateServiceAuthorisationApi(@Value("${idam.s2s-auth.url}") final String s2sUrl) {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .contract(new SpringMvcContract())
                .target(ServiceAuthorisationApi.class, s2sUrl);
    }

    @Bean
    public ServiceAuthTokenGenerator authTokenGenerator(
            @Value("${idam.s2s-auth.totp_secret}") final String secret,
            @Value("${idam.s2s-auth.microservice}") final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi) {
        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }

}
