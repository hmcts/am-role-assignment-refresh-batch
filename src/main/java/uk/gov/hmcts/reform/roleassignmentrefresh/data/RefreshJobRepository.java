package uk.gov.hmcts.reform.roleassignmentrefresh.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefreshJobRepository extends CrudRepository<RefreshJobEntity, Long> {

    List<RefreshJobEntity> findByStatusOrderByCreatedDesc(String status);

}
