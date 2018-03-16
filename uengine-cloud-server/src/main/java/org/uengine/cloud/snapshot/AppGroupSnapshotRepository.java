package org.uengine.cloud.snapshot;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "group_snapshots", path = "group_snapshots")
public interface AppGroupSnapshotRepository extends PagingAndSortingRepository<AppGroupSnapshot, Long> {

    Long removeByAppGroupId(@Param("appGroupId") Long appGroupId);

    Page<AppGroupSnapshot> findByAppGroupId(@Param("appGroupId") Long appGroupId, Pageable pageable);

    @Query("select a from AppGroupSnapshot a where a.name like CONCAT('%',:name,'%')")
    List<AppGroupSnapshot> findLikeName(@Param("name") String name, Pageable pageable);

    List<AppGroupSnapshot> findAll();
}
