package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import uk.gov.hmcts.reform.roleassignmentrefresh.config.AppConfiguration;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.EmailData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.EmailServiceImplTest.REFRESH_ENV;
import static uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.EmailServiceImplTest.MAIL_FROM;
import static uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.EmailServiceImplTest.MAIL_TO_1;
import static uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common.EmailServiceImplTest.MAIL_TO_2;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfiguration.class, EmailServiceImpl.class})
@TestPropertySource(properties = {
    "sendgrid.mail.from = " + MAIL_FROM,
    "spring.mail.to = " + MAIL_TO_1 + "," + MAIL_TO_2,
    "spring.mail.enabled = true",
    "refresh-environment = " + REFRESH_ENV
})
class EmailServiceImplTest {

    static final String MAIL_FROM = "mailfrom@example.com";
    static final String MAIL_TO_1 = "mailTo1@example.com";
    static final String MAIL_TO_2 = "mailTo2@example.com";
    static final String REFRESH_ENV = "refresh-env";

    private static final String TEST_SUBJECT = "TEST EMAIL SUBJECT";
    private static final String TEST_CONTENT = "<html/>";

    private static final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private SendGrid sendGrid;

    @MockBean
    private ITemplateEngine templateEngine;

    @Autowired
    private EmailServiceImpl sut;

    @Nested
    @TestPropertySource(properties = {
        "spring.mail.enabled = false"
    })
    class WhenMailDisabled {

        @Autowired
        private EmailServiceImpl sut;

        @Test
        void sendEmail_notEnabled() throws IOException {

            // GIVEN
            EmailData emailData = EmailData.builder()
                    .build();

            // WHEN
            sut.sendEmail(emailData);

            // THEN
            verify(sendGrid, never()).api(any());

        }

    }

    @Test
    void sendEmail_enabled() throws IOException {

        // GIVEN
        Map<String, Object> templateMap = Map.of(
                "key1", "value1",
                "key2", "value2"
        );
        EmailData emailData = EmailData.builder()
                .emailSubject(TEST_SUBJECT)
                .templateMap(templateMap)
                .build();

        when(templateEngine.process(anyString(), any())).thenReturn(TEST_CONTENT);
        when(sendGrid.api(any())).thenReturn(new Response(HttpStatus.OK.value(), null, null));

        // WHEN
        Response response = sut.sendEmail(emailData);

        // THEN
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        // verify template call: i.e. correct variables for correct template
        ArgumentCaptor<Context> contextCapture = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine, times(1)).process(anyString(), contextCapture.capture());
        Context templateContext = contextCapture.getValue();
        assertNotNull(templateContext);
        assertEquals(templateMap.size(), templateContext.getVariableNames().size());
        templateMap.keySet().forEach(key -> {
            assertTrue(templateContext.containsVariable(key));
            assertEquals(templateMap.get(key), templateContext.getVariable(key));
        });

        // verify sendGrid call:
        ArgumentCaptor<Request> requestCapture = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(requestCapture.capture());
        Request request = requestCapture.getValue();
        assertNotNull(request);
        Mail mail = mapper.readValue(request.getBody(), Mail.class);
        // MAIL FROM
        assertEquals(MAIL_FROM, mail.getFrom().getEmail());
        // MAIL TO
        List<Email> tos = mail.personalization.get(0).getTos();
        assertEquals(2, tos.size());
        tos.forEach(email -> assertTrue(List.of(MAIL_TO_1, MAIL_TO_2).contains(email.getEmail())));
        // MAIL SUBJECT
        // ... must contain ENV
        assertTrue(StringUtils.containsIgnoreCase(mail.getSubject(), REFRESH_ENV));
        // ... must contain original subject
        assertTrue(StringUtils.containsIgnoreCase(mail.getSubject(), TEST_SUBJECT));
        // MAIL CONTENT
        assertEquals(TEST_CONTENT, mail.getContent().get(0).getValue());

    }

    @Test
    void sendEmail_enabled_sendFailure() throws IOException {

        // GIVEN
        EmailData emailData = EmailData.builder()
                .emailSubject(TEST_SUBJECT)
                .build();

        when(templateEngine.process(anyString(), any())).thenReturn(TEST_CONTENT);
        when(sendGrid.api(any())).thenReturn(new Response(HttpStatus.BAD_REQUEST.value(), null, null));

        // WHEN
        Response response = sut.sendEmail(emailData);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

}
