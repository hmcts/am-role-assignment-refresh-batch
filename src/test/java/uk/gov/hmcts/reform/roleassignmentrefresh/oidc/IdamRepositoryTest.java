package uk.gov.hmcts.reform.roleassignmentrefresh.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class IdamRepositoryTest {

    @Mock
    private final IdamApi idamApi = mock(IdamApi.class);

    @Mock
    private final OIdcAdminConfiguration oidcAdminConfiguration = mock(OIdcAdminConfiguration.class);

    @Mock
    private final OAuth2Configuration oauth2Configuration = mock(OAuth2Configuration.class);

    IdamRepository idamRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        idamRepository = new IdamRepository(idamApi, oidcAdminConfiguration,
                oauth2Configuration
        );
    }

    @Test
    void getManageUserToken() {

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getUserId()).thenReturn("userid");
        when(oidcAdminConfiguration.getSecret()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
                TokenResponse("a", "1", "1", "a", "v", "v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String result = idamRepository.getUserToken();

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.isEmpty());
    }

}