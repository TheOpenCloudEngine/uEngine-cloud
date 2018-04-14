package org.uengine.cloud.app.deployjson;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.cloud.app.AppDeployJson;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "deployjson", path = "deployjson")
public interface AppDeployJsonRepository extends PagingAndSortingRepository<AppDeployJson, Long> {

    List<AppDeployJson> findAll();

    AppDeployJson findByAppNameAndStage(@Param("appName") String appName, @Param("stage") String stage);

    List<AppDeployJson> findByAppName(@Param("appName") String appName);

    void removeByAppName(@Param("appName") String appName);
}
