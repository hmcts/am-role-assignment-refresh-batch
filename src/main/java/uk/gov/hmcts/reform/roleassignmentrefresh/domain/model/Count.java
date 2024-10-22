package uk.gov.hmcts.reform.roleassignmentrefresh.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Count {

    static final String JURISDICTION_KEY = "jurisdiction";
    static final String ROLE_NAME_KEY = "roleName";
    static final String ROLE_CATEGORY_KEY = "roleCategory";
    static final String COUNT_KEY = "count";

    private String jurisdiction;
    private String roleCategory;
    private String roleName;
    private int beforeCount;
    private int afterCount;
    private int difference;

    public void populateBefore(CountResponse.CountData countData) {
        if (countData.getJurisdiction() != null) {
            this.jurisdiction = countData.getJurisdiction();
        }
        if (countData.getRoleName() != null) {
            this.roleName = countData.getRoleName();
        }
        this.roleCategory = countData.getRoleCategory();
        this.beforeCount = countData.getCount().intValue();
        this.afterCount = 0;
        // pre populate difference with negative value of count to cover case where role is not matched in after count
        this.difference = -countData.getCount().intValue();
    }

    public void populateAfter(CountResponse.CountData countData) {
        if (countData.getJurisdiction() != null) {
            this.jurisdiction = countData.getJurisdiction();
        }
        if (countData.getRoleName() != null) {
            this.roleName = countData.getRoleName();
        }
        this.roleCategory = countData.getRoleCategory();
        this.beforeCount = 0;
        this.afterCount = countData.getCount().intValue();
        this.difference = 0;
    }

    public void updateAfterCount(int afterCount) {
        this.afterCount = afterCount;
        this.difference = afterCount - this.beforeCount;
    }
}
