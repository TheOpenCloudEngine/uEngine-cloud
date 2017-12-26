package org.uengine.cloud.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.cloud.entity.AppLogEntity;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "appLogs", path = "appLogs")
public interface AppLogRepository extends PagingAndSortingRepository<AppLogEntity, Long>{

    List<AppLogEntity> findAppLogEntitiesBy();

    @Query("select ale from AppLogEntity ale where ale.appName = ?#{appName}")
    List<AppLogEntity> findAppLogEntitiesByAppName(@Param("appName") String appName);
}
