package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class UserCountServiceTest {

    RASFeignClient rasFeignClient = mock(RASFeignClient.class);

    UserCountService sut = new UserCountService(rasFeignClient);

    @Test
    void shouldReturnSuccessResponseTest() {
        Mockito.when(rasFeignClient.getUserCounts())
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("UserCountRequest"));
        ResponseEntity<Object> responseEntity = sut.getRasUserCounts();
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }


}