package org.uengine.cloud.app;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "apps", path = "apps")
public interface AppJpaRepository extends PagingAndSortingRepository<AppEntity, String> {

    List<AppEntity> findAll();

    List<AppEntity> findByAppType(@Param("appType") String appType);

    List<AppEntity> findByIam(@Param("iam") String iam);

    @Query("select a from AppEntity a where a.name like CONCAT('%',:name,'%')")
    List<AppEntity> findLikeName(@Param("name") String name);
}
