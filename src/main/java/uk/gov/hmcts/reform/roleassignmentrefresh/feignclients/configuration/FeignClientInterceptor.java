package uk.gov.hmcts.reform.roleassignmentrefresh.feignclients.configuration;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class FeignClientInterceptor {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (!requestTemplate.url().contains("health")) {
                requestTemplate.header("ServiceAuthorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbV9vcmdfcm9sZV9tYXBwaW5nX3NlcnZpY2UiLCJleHAiOjE2MjA4NDc1MTV9.5B9YGZXJTKNo50uToZMBjZHaRMXvwrLpM2PbKdxJx7z3W2VbfonRY4U-OHiGWFh4kpnMLDzc2jwMMVMRWgGlAw");
                requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJURVNUX0FNX09STV9CRUZUQUB0ZXN0LmxvY2FsIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiNjI1MzQxYmEtYjkzZC00MGZhLThhYWUtMzkwODU4YjkzYjAxIiwiaXNzIjoiaHR0cDovL2ZyLWFtOjgwODAvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IjE5NDU1NzNlLTEwMGEtNDk5Ni1iMTM2LWUwYTNjMWE1ZWU1NiIsImF1ZCI6ImFtX2RvY2tlciIsIm5iZiI6MTYyMDgzMjk2MywiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIiwiY3JlYXRlLXVzZXIiLCJtYW5hZ2UtdXNlciIsInNlYXJjaC11c2VyIiwiYXV0aG9yaXRpZXMiXSwiYXV0aF90aW1lIjoxNjIwODMyOTYzLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTYyMDg2MTc2MywiaWF0IjoxNjIwODMyOTYzLCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiODQ0MjVhNjEtYWNlZC00MmFjLWI2ODAtOTU2MWY0MGI1M2NhIn0.HzCk_wN0skHZfCOBoK5EeL_GWs12oXu9CH_8s9R_l87mbqBuD5t33ONV5hXO-oWCqPKiQ0CrZjVs_2eA19iZTR192De0_8mUt2gtWSVTYR8s5bjTX-LHPAbsEdWycdplI5rF3I-ZWLx-l6CbM0HkFxrhVTj6QSy83UVXPXgl0aUDF_9PdC3TZNQ1a-6cyvkgEocnqkhUHYvNmLH2wqw_i7ffdbrA41kJFKU3UIzbizcdnu_RQInwKmncznUm59CPD1TdtaUoe9lgOJkek3bKnKYwpdpaPm0bJpYQLj1a-BeCS-JkhPXwC6dL_npTF_fZlvhGd1mBBe4AnCUY9gDmGg" );
                requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");
            }
        };
    }


}
