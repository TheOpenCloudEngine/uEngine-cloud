package org.uengine.cloud.debug;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "debug", path = "debug")
public interface AppDebugPortRepository extends PagingAndSortingRepository<AppDebugPort, String> {

    List<AppDebugPort> findAll();
}
