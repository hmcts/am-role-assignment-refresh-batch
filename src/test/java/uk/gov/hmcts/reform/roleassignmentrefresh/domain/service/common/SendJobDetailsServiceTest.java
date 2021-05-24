package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.common;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.UserRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SendJobDetailsServiceTest {

    ORMFeignClient ormFeignClient = mock(ORMFeignClient.class);

    SendJobDetailsService sut = new SendJobDetailsService(ormFeignClient);

    @Test
    void sendToRoleAssignmentBatchServiceTest() {
        Mockito.when(ormFeignClient.sendJobToRoleAssignmentBatchService(Mockito.any(),
                ArgumentMatchers.any(UserRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("UserRequest"));
        ResponseEntity<Object> responseEntity = sut.sendToRoleAssignmentBatchService(1L, UserRequest.builder().build());
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }


}