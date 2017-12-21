package org.uengine.cloud.app;


import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


import java.util.List;

/**
 * Created by uengine on 2017. 8. 1..
 */
@RepositoryRestResource(collectionResourceRel = "appLogs", path = "appLogs")
//@Repository
public interface AppLogRepository extends PagingAndSortingRepository<AppLogEntity, Long> {
//public interface AppLogRepository {

    List<AppLogEntity> appLogList();

}


