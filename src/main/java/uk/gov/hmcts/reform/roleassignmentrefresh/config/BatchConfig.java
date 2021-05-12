package uk.gov.hmcts.reform.roleassignmentrefresh.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.roleassignmentrefresh.task.RefreshORMRules;

@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {

    @Value("${refresh-orm-records}")
    String taskParent;

    @Value("${batchjob-name}")
    String jobName;

    @Bean
    public Step stepOrchestration(@Autowired StepBuilderFactory steps,
                                  @Autowired RefreshORMRules refreshORMRules) {
        return steps.get(taskParent)
                    .tasklet(refreshORMRules)
                    .build();
    }

   @Bean
    public Job runRoutesJob(@Autowired JobBuilderFactory jobs,
                            @Autowired StepBuilderFactory steps,
                            @Autowired RefreshORMRules refreshORMRules) {
        return jobs.get(jobName)
                   .incrementer(new RunIdIncrementer())
                   .start(stepOrchestration(steps, refreshORMRules))
                   .build();
    }
}
