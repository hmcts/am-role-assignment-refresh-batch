package uk.gov.hmcts.reform.roleassignmentrefresh.domain.service.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.List;

@Slf4j
@Service
public class RefreshJobsOrchestrator {


    public void processRefreshJobs() {
        long startTime = System.currentTimeMillis();
        // Get new job entries for refresh

        log.info(" >> Refresh Batch Job execution finished at {} . Time taken = {} milliseconds",
                System.currentTimeMillis(), Math.subtractExact(System.currentTimeMillis(), startTime)
        );
    }
}
