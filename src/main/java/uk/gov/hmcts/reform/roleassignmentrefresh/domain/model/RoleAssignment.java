package uk.gov.hmcts.reform.roleassignmentrefresh.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignment {

    private UUID id; //this will be generated by RAS while saving request entity.
    private ActorIdType actorIdType; // will be set to IDAM
    private String actorId; // will be set as per User Id
    private RoleType roleType; // will be set to ORGANISATIONAL for case-worker
    private String roleName; // will be set as per mapping rule
    private Classification classification; // will be set to PUBLIC for case-worker
    private GrantType grantType; // will be set to STANDARD for case-worker
    private RoleCategory roleCategory; // will be set to STAFF for case-worker
    private boolean readOnly; // will be set to false for case-worker
    private ZonedDateTime beginTime; // will be set to null for case-worker
    private ZonedDateTime endTime; // will be set to null for case-worker
    // there are only 2 attributes identified 1)jurisdiction=IA(set by mapping rule)
    // and primaryLocation=<Extract from Staff user>
    private Map<String, JsonNode> attributes;
    private JsonNode notes; //this would be empty for case-worker and reserved for future requirements.
    private List<String> authorisations; // this is not applicable for case-worker

    private String process; //need to map from request
    private String reference; //need to map from request
    private Integer statusSequence; //this will be populated from status entity. Need to extend status entity.
    private Status status; //this will be set by app default = created
    private LocalDateTime created; //this will be set by app
    private String log; //this will be set app based on drool validation rule name on individual assignments.

}
