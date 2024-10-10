package uk.gov.hmcts.reform.roleassignmentrefresh.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshJob {
    private String jobId;
    private String roleCategory;
    private String jurisdiction;
}
