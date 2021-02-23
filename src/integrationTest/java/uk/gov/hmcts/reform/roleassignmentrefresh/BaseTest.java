package uk.gov.hmcts.reform.roleassignmentrefresh;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.BeforeClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
public abstract class BaseTest {

    protected static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

}