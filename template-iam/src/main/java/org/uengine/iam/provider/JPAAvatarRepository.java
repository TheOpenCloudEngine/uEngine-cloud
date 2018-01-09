package org.uengine.iam.provider;

/**
 * Created by uengine on 2017. 12. 21..
 */

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "avatar", path = "avatar")
public interface JPAAvatarRepository extends PagingAndSortingRepository<JPAAvatarEntity, Long> {

    JPAAvatarEntity findByUserName(@Param("userName") String userName);
}