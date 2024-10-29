package uk.gov.hmcts.reform.roleassignmentrefresh.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Value("${sendgrid.key}")
    String sendGridKey;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendGridKey);
    }
}
