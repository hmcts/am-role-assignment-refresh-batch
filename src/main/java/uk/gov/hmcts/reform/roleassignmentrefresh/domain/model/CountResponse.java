package uk.gov.hmcts.reform.roleassignmentrefresh.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountResponse {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CountData {
        String jurisdiction;

        String roleCategory;

        String roleName;

        BigInteger count;
    }

    @JsonProperty("OrgUserCountByJurisdiction")
    private CountData[] orgUserCountByJurisdiction;

    @JsonProperty("OrgUserCountByJurisdictionAndRoleName")
    private CountData[] orgUserCountByJurisdictionAndRoleName;
}
