package uk.gov.hmcts.reform.roleassignmentrefresh.oidc;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.roleassignmentrefresh.constants.RefreshConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityUtilsTest {

    @Mock
    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);

    @Mock
    SecurityContext securityContext = mock(SecurityContext.class);
    @Mock
    IdamRepository idamRepository = mock(IdamRepository.class);

    @InjectMocks
    private final SecurityUtils securityUtils = new SecurityUtils(
            authTokenGenerator,
            idamRepository
    );

    private final String serviceAuthorization = "Bearer eyJhbGciOiJIUzUxMiJ9"
            + ".eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTQ2ODQ5MTF9"
            + ".LH3aiNniHNMlTwuSdzgRic9sD_4inQv5oUqJ0kkRKVasS4RfhIz2tRdttf-sSMkUga1p1teOt2iCq4BQBDS7KA";
    private final String serviceAuthorizationNoBearer = "eyJhbGciOiJIUzUxMiJ9"
            + ".eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTQ2ODQ5MTF9"
            + ".LH3aiNniHNMlTwuSdzgRic9sD_4inQv5oUqJ0kkRKVasS4RfhIz2tRdttf-sSMkUga1p1teOt2iCq4BQBDS7KA";
    private static final String USER_ID = "21334a2b-79ce-44eb-9168-2d49a744be9c";
    private final String userAuthorization = "Bearer eyJhbGciOiJIUzUxMiJ9"
            + ".eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTQ2ODQ5MTF9"
            + ".LH3aiNniHNMlTwuSdzgRic9sD_4inQv5oUqJ0kkRKVasS4RfhIz2tRdttf-sSMkUga1p1teOt2iCq4BQBDS7KB";

    private void mockSecurityContextData() {
        List<String> collection = new ArrayList<>();
        collection.add("string");
        Map<String, Object> headers = new HashMap<>();
        headers.put("header", "head");
        when(authTokenGenerator.generate()).thenReturn(serviceAuthorization);
        when(idamRepository.getUserToken()).thenReturn(userAuthorization);
    }

    @BeforeEach
    public void setUp() {
        mockSecurityContextData();
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void getUserToken() {
        String result = securityUtils.getUserToken();
        assertNotNull(result);
        assertTrue(result.contains("eyJhbG"));
    }

    @Test
    void getServiceToken() {
        String result = securityUtils.getServiceToken();
        assertNotNull(result);
        assertTrue(result.contains("eyJhbG"));
    }

    @Test
    void getUserTokenNoContext() {
        when(securityContext.getAuthentication()).thenReturn(null);
        when(idamRepository.getUserToken()).thenReturn(serviceAuthorization);
        String result = securityUtils.getUserToken();
        assertNotNull(result);
        assertTrue(result.contains("eyJhbG"));
    }

    @Test
    void getServiceAuthorizationHeader() {
        HttpHeaders result = securityUtils.authorizationHeaders();
        assertNotNull(result);
        assertTrue(result.containsKey(RefreshConstants.SERVICE_AUTHORIZATION));
    }

    @Test
    void getAuthorizationHeaders() {
        HttpHeaders result = securityUtils.authorizationHeaders();
        assertEquals(serviceAuthorization, Objects.requireNonNull(
                result.get(RefreshConstants.SERVICE_AUTHORIZATION)).get(0));
        assertNotNull(result.get(HttpHeaders.AUTHORIZATION));
    }
}
