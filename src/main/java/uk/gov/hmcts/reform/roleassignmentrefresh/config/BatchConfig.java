package uk.gov.hmcts.reform.roleassignmentrefresh.config;

import com.launchdarkly.sdk.server.LDClient;
import feign.Feign;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.roleassignmentrefresh.task.RefreshORMRules;

@Configuration
@Slf4j
public class BatchConfig {

    @Value("${refresh-orm-records}")
    String taskParent;

    @Value("${batchjob-name}")
    String jobName;

    @Bean
    public Step stepOrchestration(JobRepository jobRepository, RefreshORMRules refreshORMRules,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder(taskParent, jobRepository)
                .tasklet(refreshORMRules, transactionManager)
                .build();
    }

    @Bean
    public Job runRoutesJob(JobRepository jobRepository,
                            RefreshORMRules refreshORMRules, PlatformTransactionManager transactionManager) {
        return new JobBuilder(jobName, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(stepOrchestration(jobRepository, refreshORMRules, transactionManager))
                .build();
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

    @Bean
    public LDClient ldClient(@Value("${launchdarkly.sdk.key}") String sdkKey) {

        return new LDClient(sdkKey);
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}
