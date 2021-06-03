package uk.gov.hmcts.reform.roleassignmentrefresh.advice.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.reform.roleassignmentrefresh.constants.RefreshConstants;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException  extends RuntimeException {

    private static final long serialVersionUID = 7L;

    public ForbiddenException(String message) {
        super(String.format(RefreshConstants.FORBIDDEN + ": %s", message));
    }

    public ForbiddenException(UUID message) {
        super(message.toString());
    }
}
