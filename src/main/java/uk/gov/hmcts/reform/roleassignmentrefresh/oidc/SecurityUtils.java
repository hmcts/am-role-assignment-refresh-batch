package uk.gov.hmcts.reform.roleassignmentrefresh.oidc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.roleassignmentrefresh.constants.RefreshConstants;

@Service
@Slf4j
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamRepository idamRepository;

    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator, IdamRepository idamRepository) {
        this.authTokenGenerator = authTokenGenerator;
        this.idamRepository = idamRepository;
    }

    public HttpHeaders authorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(RefreshConstants.SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.add(HttpHeaders.AUTHORIZATION, RefreshConstants.BEARER + getUserToken());

        return headers;
    }

    public String getUserToken() {
        return idamRepository.getUserToken();
    }

    public String getServiceToken() {
        return authTokenGenerator.generate();
    }
}