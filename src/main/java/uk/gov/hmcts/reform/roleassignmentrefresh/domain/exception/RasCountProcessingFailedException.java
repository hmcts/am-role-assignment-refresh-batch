package uk.gov.hmcts.reform.roleassignmentrefresh.domain.exception;

public class RasCountProcessingFailedException extends RuntimeException {

    public RasCountProcessingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RasCountProcessingFailedException(Throwable cause) {
        super(cause);
    }
}
