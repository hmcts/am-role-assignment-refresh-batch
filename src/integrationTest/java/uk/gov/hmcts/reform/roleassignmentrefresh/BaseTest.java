package uk.gov.hmcts.reform.roleassignmentrefresh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;

@Configuration
public abstract class BaseTest {

    protected static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @TestConfiguration
    static class Configuration {
        Connection connection;

        @Bean
        public PostgresTestContainer embeddedPostgres() {
            return PostgresTestContainer
                    .builder()
                    .start();
        }

        @Bean
        public DataSource dataSource(@Autowired PostgresTestContainer pg) throws Exception {

            final Properties props = new Properties();
            // Instruct JDBC to accept JSON string for JSONB
            props.setProperty("stringtype", "unspecified");
            props.setProperty("user", "postgres");
            connection = DriverManager.getConnection(pg.getJdbcUrl("postgres"), props);
            return new SingleConnectionDataSource(connection, true);
        }
        
        @PreDestroy
        public void contextDestroyed() throws SQLException {
            if (connection != null) {
                connection.close();
            }
        }
    }

}