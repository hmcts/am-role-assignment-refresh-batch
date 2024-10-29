package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.CountResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class UserCountServiceTest {

    RASFeignClient rasFeignClient = mock(RASFeignClient.class);

    UserCountService sut = new UserCountService(rasFeignClient);

    @Test
    void getRasUserCountsReturnSuccessTest() {
        Mockito.when(rasFeignClient.getUserCounts())
                .thenReturn(new ResponseEntity<>(new CountResponse(), HttpStatus.OK));
        ResponseEntity<CountResponse> countResponse = sut.getRasUserCounts();
        assertEquals(HttpStatus.OK, countResponse.getStatusCode());
    }
}