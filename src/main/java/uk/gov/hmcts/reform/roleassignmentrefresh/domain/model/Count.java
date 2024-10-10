package uk.gov.hmcts.reform.roleassignmentrefresh.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
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

    public void populateBefore(JsonNode node) {
        if (node.get(JURISDICTION_KEY) != null) {
            this.jurisdiction = node.get(JURISDICTION_KEY).asText();
        }
        if (node.get(ROLE_NAME_KEY) != null) {
            this.roleName = node.get(ROLE_NAME_KEY).asText();
        }

        this.roleCategory = node.get(ROLE_CATEGORY_KEY).asText();
        this.beforeCount = node.get(COUNT_KEY).asInt();
        this.afterCount = 0;
        this.difference = 0;
    }

    public void populateAfter(JsonNode node) {
        if (node.get(JURISDICTION_KEY) != null) {
            this.jurisdiction = node.get(JURISDICTION_KEY).asText();
        }
        if (node.get(ROLE_NAME_KEY) != null) {
            this.roleName = node.get(ROLE_NAME_KEY).asText();
        }

        this.roleCategory = node.get(ROLE_CATEGORY_KEY).asText();
        this.beforeCount = 0;
        this.afterCount = node.get(COUNT_KEY).asInt();
        this.difference = 0;
    }

    public void updateAfterCount(int afterCount) {
        this.afterCount = afterCount;
        this.difference = afterCount - this.beforeCount;
    }
}
