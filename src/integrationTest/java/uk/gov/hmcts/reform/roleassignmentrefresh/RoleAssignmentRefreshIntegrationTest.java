package uk.gov.hmcts.reform.roleassignmentrefresh;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.roleassignmentrefresh.task.DeleteExpiredRecords;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = BaseTest.class)
public class RoleAssignmentRefreshIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentRefreshIntegrationTest.class);

    private DeleteExpiredRecords sut;

    @Before
    public void setUp() {
    }

    @Test
    public void dummyTest() {
        int testInt = 5;
        assertEquals(6, testInt + 1);
    }
}
