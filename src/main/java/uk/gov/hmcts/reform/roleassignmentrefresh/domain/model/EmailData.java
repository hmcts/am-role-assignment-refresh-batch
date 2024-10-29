package uk.gov.hmcts.reform.roleassignmentrefresh.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class EmailData {
    private List<String> emailTo;
    private String emailSubject;
    Map<String, Object> templateMap;
}
