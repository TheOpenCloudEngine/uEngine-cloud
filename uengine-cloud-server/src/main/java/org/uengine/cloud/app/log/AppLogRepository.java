package org.uengine.cloud.app.log;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "appLogs", path = "appLogs")
public interface AppLogRepository extends PagingAndSortingRepository<AppLogEntity, Long> {

    Page<AppLogEntity> findByAppName(@Param("appName") String appName, Pageable pageable);

    Page<AppLogEntity> findByUpdater(@Param("updater") String updater, Pageable pageable);

    Page<AppLogEntity> findByAppNameAndStatus(@Param("userName") String userName, @Param("status") AppLogStatus status, Pageable pageable);


}
