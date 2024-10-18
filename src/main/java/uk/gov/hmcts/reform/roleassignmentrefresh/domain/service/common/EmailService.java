package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import com.sendgrid.Response;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.EmailData;

public interface EmailService {
    public Response sendEmail(EmailData emailData);
}
