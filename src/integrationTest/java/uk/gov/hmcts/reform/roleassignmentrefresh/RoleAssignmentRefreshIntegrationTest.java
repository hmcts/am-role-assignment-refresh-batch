package uk.gov.hmcts.reform.roleassignmentrefresh;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = {BaseTest.class})
public class RoleAssignmentRefreshIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentRefreshIntegrationTest.class);

    private JdbcTemplate template;

    private static final String GET_REFRESH_JOBS_QUERY = "select job_id from public.refresh_jobs rj where rj.status "
            + "= ? and rj.linked_job_id = 0";
    private static final String GET_REFRESH_JOBS_FOR_FAILED_RECORDS_QUERY = "select linked_job_id from "
            + "public.refresh_jobs rj where rj.status = ? and rj.linked_job_id != 0";
    private static final String GET_FAILED_USER_IDS_QUERY = "select user_ids from public.refresh_jobs rj where "
            + "rj.job_id = ?";
    private static final String COUNT_REFRESH_JOBS_QUERY = "SELECT count(1) AS n FROM refresh_jobs";
    private static String REFRESH_JOB_STATUS = "NEW";
    private static String USER_ID1 = "778520f2-1f10-4270-b5d6-9404e274a3f2";
    private static String USER_ID2 = "778520f2-1f10-4270-b5d6-9404e274a3f1";

    @Autowired
    private DataSource ds;

    @Before
    public void setUp() {
        template = new JdbcTemplate(ds);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
            {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldGetAllRecordsFromRefreshJobs() {
        logger.info(" Record count in refresh_jobs table : {}", getTotalRefreshJobsCount());
        int count = getTotalRefreshJobsCount();
        logger.info(" Total Jobs to refresh from refresh_job table...{} ", count);
        assertEquals("Job Id", 4, count);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
            {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldGetNewRecordsFromRefreshJobs() {
        int jobId = getNewRefreshJobs(new Object[]{REFRESH_JOB_STATUS});
        logger.info(" Job Id to refresh jobs from refresh_job table...{} ", jobId);
        assertEquals("Job Id", 1, jobId);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
            {"classpath:sql/insert_refresh_jobs.sql"})
    public void shouldGetNewRefreshJobsForFailedUsers() throws SQLException {
        int linkedJobId = getLinkedJobIds();
        logger.info(" Linked job Id fetched from refresh_job table...{} ", linkedJobId);
        assertEquals("linked Job Id", 3, linkedJobId);
        List<String> failedUserIds = getUserIdsBasedOnLinkedJobId(linkedJobId);
        logger.info(" userIds fetched from refresh_job table...{} ", failedUserIds);
        assertEquals("Failed User Ids count", 2, failedUserIds.size());
        assertEquals("Failed User Id", USER_ID1, failedUserIds.get(0));
        assertEquals("Failed User Id", USER_ID2, failedUserIds.get(1));
    }

    @NotNull
    private List<String> getUserIdsBasedOnLinkedJobId(int linkedJobId) throws SQLException {
        Array userIds = template.queryForObject(GET_FAILED_USER_IDS_QUERY, new Object[]{linkedJobId}, Array.class);
        return Arrays.asList((String[]) userIds.getArray());
    }

    private Integer getLinkedJobIds() {
        return template.queryForObject(GET_REFRESH_JOBS_FOR_FAILED_RECORDS_QUERY, new Object[]{REFRESH_JOB_STATUS},
                Integer.class);
    }

    private Integer getNewRefreshJobs(Object[] parameters) {
        return template.queryForObject(GET_REFRESH_JOBS_QUERY, parameters, Integer.class);
    }

    private Integer getTotalRefreshJobsCount() {
        return template.queryForObject(COUNT_REFRESH_JOBS_QUERY, Integer.class);
    }
}
