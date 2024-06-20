package uk.gov.hmcts.reform.roleassignmentrefresh.oidc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.roleassignmentrefresh.constants.RefreshConstants;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    IdamRepository idamRepository;

    @InjectMocks
    private SecurityUtils securityUtils;

    private static final String TEST_SERVICE_AUTH_TOKEN = "DUMMY_SERVICE_AUTH_TOKEN";
    private static final String TEST_USER_AUTH_TOKEN = "DUMMY_USER_AUTH_TOKEN";

    @Test
    void getServiceToken() {

        // GIVEN
        stubServiceAuthTokenCall();

        // WHEN
        String result = securityUtils.getServiceToken();

        // THEN
        assertEquals(TEST_SERVICE_AUTH_TOKEN, result);

    }

    @Test
    void getUserToken() {

        // GIVEN
        stubUserAuthTokenCall();

        // WHEN
        String result = securityUtils.getUserToken();

        // THEN
        assertEquals(TEST_USER_AUTH_TOKEN, result);

    }

    @Test
    void getAuthorizationHeader() {

        // GIVEN
        stubServiceAuthTokenCall();
        stubUserAuthTokenCall();

        // WHEN
        HttpHeaders result = securityUtils.authorizationHeaders();

        // THEN
        assertNotNull(result);
        assertTrue(result.containsKey(RefreshConstants.SERVICE_AUTHORIZATION));
        assertEquals(
                TEST_SERVICE_AUTH_TOKEN,
                Objects.requireNonNull(result.get(RefreshConstants.SERVICE_AUTHORIZATION)).get(0)
        );
        assertTrue(result.containsKey(HttpHeaders.AUTHORIZATION));
        assertEquals(
                RefreshConstants.BEARER + TEST_USER_AUTH_TOKEN,
                Objects.requireNonNull(result.get(HttpHeaders.AUTHORIZATION)).get(0)
        );
    }

    private void stubServiceAuthTokenCall() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    private void stubUserAuthTokenCall() {
        when(idamRepository.getUserToken()).thenReturn(TEST_USER_AUTH_TOKEN);
    }

}
