package uk.gov.hmcts.reform.roleassignmentbatch.helper;

import uk.gov.hmcts.reform.roleassignmentrefresh.data.RefreshJobEntity;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.enums.Status;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("HideUtilityClassConstructor")
public class TestDataBuilder {
    private static String USER_ID1 = "7c12a4bc-450e-4290-8063-b387a5d5e0b7";
    private static String USER_ID2 = "21334a2b-79ce-44eb-9168-2d49a744be9c";

    public static List<RefreshJobEntity> buildRefreshJobEntities(String status) {
        return List.of(buildRefreshJobEntity(status));
    }

    public static RefreshJobEntity buildNewWithLinkedJobRefreshJobEntities() {
        return RefreshJobEntity.builder().jobId(2L).jurisdiction("IA").roleCategory("JUDICIAL").linkedJobId(1L)
                .status(Status.NEW.name()).build();
    }

    public static RefreshJobEntity buildRefreshJobEntity(String status) {
        return RefreshJobEntity.builder().jobId(1L).jurisdiction("IA").roleCategory("JUDICIAL")
                .status(status).build();
    }

    public static Optional<RefreshJobEntity> buildOptionalRefreshJobEntity(String status) {
        RefreshJobEntity refreshJobEntity = buildRefreshJobEntity(status);
        refreshJobEntity.setUserIds(new String[]{USER_ID1, USER_ID2});
        return Optional.of(refreshJobEntity);
    }

}
