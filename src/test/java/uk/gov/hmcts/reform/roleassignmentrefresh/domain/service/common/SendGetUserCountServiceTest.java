package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SendGetUserCountServiceTest {

    RASFeignClient rasFeignClient = mock(RASFeignClient.class);

    SendGetUserCountService sut = new SendGetUserCountService(rasFeignClient);

    @Test
    void sendToRoleAssignmentBatchServiceTest() {
        Mockito.when(rasFeignClient.sendGetUserCountToRoleAssignmentService())
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("UserCountRequest"));
        ResponseEntity<Object> responseEntity = sut.sendGetUserCountToRoleAssignmentService();
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }


}