package uk.gov.hmcts.reform.roleassignmentrefresh.controller;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.service.RoleAssignmentService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.nio.charset.StandardCharsets;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WelcomeControllerTest {

    private MockMvc mockMvc;

    private RoleAssignmentService rasMock = mock(RoleAssignmentService.class);

    WelcomeController sut = new WelcomeController(rasMock);

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @BeforeEach
    public void setUp() {
        this.mockMvc = standaloneSetup(this.sut).build();
    }

    @Test
    void welcome() throws Exception {
        final String url = "/welcome";
        final MvcResult result = mockMvc.perform(get(url).contentType(JSON_CONTENT_TYPE))
                .andExpect(status().is(200))
                .andReturn();
    }
}