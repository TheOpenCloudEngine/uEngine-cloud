package org.uengine.cloud.group;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "groups", path = "groups")
public interface AppGroupJpaRepository extends PagingAndSortingRepository<AppGroup, Long> {

    List<AppGroup> findAll();
}
