package org.uengine.cloud.app;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "apps", path = "apps")
public interface AppEntityRepository extends PagingAndSortingRepository<AppEntity, String> {

    List<AppEntity> findAll();

    @Query("select a.number from AppEntity a")
    List<Integer> findAllAppNumbers();

    @Query("select a.name from AppEntity a")
    List<String> findAllAppNames();

    AppEntity findByProjectId(@Param("projectId") int projectId);

    @Query("select a from AppEntity a where a.name like CONCAT('%',:name,'%') and a.memberIds like CONCAT('%',:gitlabId,'%')")
    Page<AppEntity> findLikeNameAndGitlabId(@Param("name") String name, @Param("gitlabId") String gitlabId, Pageable pageable);
}
