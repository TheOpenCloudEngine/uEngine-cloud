package org.uengine.cloud.app.snapshot;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "snapshots", path = "snapshots")
public interface AppSnapshotRepository extends PagingAndSortingRepository<AppSnapshot, Long> {

    Long removeByAppName(@Param("appName") String appName);

    Page<AppSnapshot> findByAppName(@Param("appName") String appName, Pageable pageable);

    @Query("select a from AppSnapshot a where a.name like CONCAT('%',:name,'%')")
    List<AppSnapshot> findLikeName(@Param("name") String name, Pageable pageable);

    List<AppSnapshot> findAll();

    List<AppSnapshot> findByAppGroupSnapshotId(@Param("appGroupSnapshotId") String AppGroupSnapshotId);


}
