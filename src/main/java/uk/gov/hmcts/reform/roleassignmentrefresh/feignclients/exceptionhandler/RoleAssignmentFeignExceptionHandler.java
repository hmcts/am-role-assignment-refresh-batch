package uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.exceptionhandler;

import feign.Response;
import feign.codec.ErrorDecoder;

public class RoleAssignmentFeignExceptionHandler implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:

            case 404:

            default:
                return new Exception("The Role Assignment Batch service application is down " + response.toString());
        }
    }
}
