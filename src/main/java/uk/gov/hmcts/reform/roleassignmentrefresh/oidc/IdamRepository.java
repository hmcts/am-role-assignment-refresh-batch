package uk.gov.hmcts.reform.roleassignmentrefresh.oidc;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;

@Component
@Slf4j
public class IdamRepository {

    private IdamApi idamApi;
    private OIdcAdminConfiguration oidcAdminConfiguration;
    private OAuth2Configuration oauth2Configuration;

    @Autowired
    public IdamRepository(IdamApi idamApi,
                          OIdcAdminConfiguration oidcAdminConfiguration,
                          OAuth2Configuration oauth2Configuration) {
        this.idamApi = idamApi;
        this.oidcAdminConfiguration = oidcAdminConfiguration;
        this.oauth2Configuration = oauth2Configuration;
    }


    public String getUserToken() {
        TokenRequest tokenRequest = new TokenRequest(
                oauth2Configuration.getClientId(),
                oauth2Configuration.getClientSecret(),
                "password",
                "",
                oidcAdminConfiguration.getUserId(),
                oidcAdminConfiguration.getSecret(),
                oidcAdminConfiguration.getScope(),
                "4",
                ""
        );
        log.info("tokenrequest params: " + tokenRequest.getClientId());
        log.info("tokenrequest params: " + tokenRequest.getClientSecret());
        log.info("tokenrequest params: " + tokenRequest.getScope());
        log.info("tokenrequest params: " + tokenRequest.getPassword());
        log.info("tokenrequest params: " + tokenRequest.getUsername());

        TokenResponse tokenResponse = idamApi.generateOpenIdToken(tokenRequest);
        return tokenResponse.accessToken;
    }

}
