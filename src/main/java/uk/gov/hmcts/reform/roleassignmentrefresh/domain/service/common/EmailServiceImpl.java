package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.exception.EmailSendFailedException;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.EmailData;

import java.util.List;

/**
 * This class sends emails to intended recipients for batch process with summary of outcome.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    static final String TEMPLATE_COUNT_EMAIL = "count-email.html";

    private final String mailFrom;
    private final List<String> mailTo;
    private final boolean mailEnabled;
    private final String environmentName;

    private final SendGrid sendGrid;
    private final ITemplateEngine templateEngine;

    @Autowired
    public EmailServiceImpl(SendGrid sendGrid,
                            ITemplateEngine templateEngine,
                            @Value("${sendgrid.mail.from}") String mailFrom,
                            @Value("${spring.mail.to}") List<String> mailTo,
                            @Value("${spring.mail.enabled:false}") boolean mailEnabled,
                            @Value("${refresh-environment}") String environmentName) {
        this.sendGrid = sendGrid;
        this.templateEngine = templateEngine;
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
        this.mailEnabled = mailEnabled;
        this.environmentName = environmentName;
    }

    /**
     * Generic Method is used to send mail notification to the caller.
     *
     * @param emailData EmailData as parameter
     * @return SendGrid response
     */
    @Override
    @SneakyThrows
    public Response sendEmail(EmailData emailData) {
        Response response = null;
        if (mailEnabled) {
            String concatEmailSubject = environmentName.toUpperCase().concat("::" + emailData.getEmailSubject());
            var personalization = new Personalization();
            mailTo.forEach(email -> personalization.addTo(new Email(email)));
            emailData.setEmailTo(mailTo);
            emailData.setEmailSubject(concatEmailSubject);
            emailData.setRunId(emailData.getRunId());

            emailData.setTemplateMap(emailData.getTemplateMap());
            Context context = new Context();
            context.setVariables(emailData.getTemplateMap());
            String process = templateEngine.process(TEMPLATE_COUNT_EMAIL, context);
            Content content = new Content("text/html", process);

            Mail mail = new Mail();
            mail.setFrom(new Email(mailFrom));
            mail.setSubject(concatEmailSubject);
            mail.addContent(content);
            mail.addPersonalization(personalization);
            var request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            response = sendGrid.api(request);
            if (response != null && !HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
                EmailSendFailedException emailSendFailedException = new EmailSendFailedException(
                        new HttpException(String.format(
                                "SendGrid returned a non-success response (%d); body: %s",
                                response.getStatusCode(),
                                response.getBody()
                        )));
                log.error("", emailSendFailedException);
            }
        }
        return response;
    }

}
