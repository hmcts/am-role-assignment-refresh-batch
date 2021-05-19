package uk.gov.hmcts.reform.roleassignmentrefresh.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest implements Serializable {
    private String[] userIds;
}