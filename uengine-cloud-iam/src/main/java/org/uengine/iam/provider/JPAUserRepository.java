package org.uengine.iam.provider;

/**
 * Created by uengine on 2017. 12. 21..
 */

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel = "user", path = "user")
public interface JPAUserRepository extends PagingAndSortingRepository<JPAUserEntity, Long> {

    JPAUserEntity findByUserName(@Param("userName") String userName);


    @Query("select u from JPAUserEntity u where u.userName like CONCAT('%',:userName,'%')")
    Page<JPAUserEntity> findLikeUserName(@Param("userName") String userName, Pageable pageable);


    @Query("select u from JPAUserEntity u where u.userName like CONCAT('%@',:tenantId,'%') and u.metaDataString like CONCAT('%', :scope, '%')")
    Page<JPAUserEntity> findLikeTenantIdAndScope(@Param("tenantId") String tenantId, @Param("scope") String scope, Pageable pageable);


    JPAUserEntity findByUserNameAndUserPassword(@Param("userName") String userName, @Param("userPassword") String userPassword);

}