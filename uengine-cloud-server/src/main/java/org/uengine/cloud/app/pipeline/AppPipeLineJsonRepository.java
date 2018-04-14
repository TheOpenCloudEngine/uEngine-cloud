package org.uengine.cloud.app.pipeline;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.cloud.app.AppPipeLineJson;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "pipeline", path = "pipeline")
public interface AppPipeLineJsonRepository extends PagingAndSortingRepository<AppPipeLineJson, String> {

    List<AppPipeLineJson> findAll();

    AppPipeLineJson findByAppName(@Param("appName") String appName);

    Long removeByAppName(@Param("appName") String appName);
}
