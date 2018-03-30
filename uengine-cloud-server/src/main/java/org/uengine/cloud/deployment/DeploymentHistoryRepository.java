package org.uengine.cloud.deployment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.cloud.log.AppLogEntity;
import org.uengine.cloud.log.AppLogStatus;

@RepositoryRestResource(collectionResourceRel = "deploymentHistory", path = "deploymentHistory")
public interface DeploymentHistoryRepository extends PagingAndSortingRepository<DeploymentHistoryEntity, Long> {

    Page<DeploymentHistoryEntity> findByAppNameAndStage(
            @Param("appName") String appName,
            @Param("stage") String stage,
            Pageable pageable);
}
