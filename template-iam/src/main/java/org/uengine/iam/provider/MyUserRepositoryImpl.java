package org.uengine.iam.provider;


import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.uengine.iam.oauthuser.OauthAvatar;
import org.uengine.iam.oauthuser.OauthUser;
import org.uengine.iam.oauthuser.OauthUserPage;
import org.uengine.iam.oauthuser.OauthUserRepository;

import java.util.ArrayList;
import java.util.List;

public class MyUserRepositoryImpl implements OauthUserRepository {

    @Autowired
    private JPAUserRepository userRepository;

    @Autowired
    private JPAAvatarRepository avatarRepository;

    /**
     * 사용자를 생성한다.
     * @param oauthUser
     * @return
     */
    @Override
    public OauthUser insert(OauthUser oauthUser) {
        JPAUserEntity entity = this.toMyModel(oauthUser);
        return this.toOauthUser(userRepository.save(entity));
    }

    /**
     * 사용자를 업데이트한다.
     * @param oauthUser
     * @return
     */
    @Override
    public OauthUser update(OauthUser oauthUser) {
        JPAUserEntity toUpdate = userRepository.findByUserName(oauthUser.getUserName());

        //패스워드가 들어오면 덮어쓰기
        if(!StringUtils.isEmpty(oauthUser.getUserPassword())){
            toUpdate.setUserPassword(oauthUser.getUserPassword());
        }

        //메타데이터는 덮어쓰기
        toUpdate.setMetaData(oauthUser.getMetaData());

        return this.toOauthUser(userRepository.save(toUpdate));
    }

    /**
     * userName 으로 사용자를 찾는다.
     * @param userName
     * @return
     */
    @Override
    public OauthUser findByUserName(String userName) {
        return this.toOauthUser(userRepository.findByUserName(userName));
    }


    /**
     * searchKey 로 userName 을 Like 검색한다.
     * @param searchKey
     * @param pageable
     * @return OauthUserPage
     */
    @Override
    public OauthUserPage findLikeUserName(String searchKey, Pageable pageable) {
        Page<JPAUserEntity> all = null;
        if (StringUtils.isEmpty(searchKey)) {
            all = userRepository.findAll(pageable);
        } else {
            all = userRepository.findLikeUserName(searchKey, pageable);
        }
        OauthUserPage page = new OauthUserPage();
        page.setTotal(all.getTotalElements());

        List<OauthUser> oauthUserList = new ArrayList<>();
        List<JPAUserEntity> content = all.getContent();
        for (int i = 0; i < content.size(); i++) {
            oauthUserList.add(this.toOauthUser(content.get(i)));
        }
        page.setOauthUserList(oauthUserList);
        return page;
    }

    /**
     * userName 과 userPassword 로 사용자를 찾는다.
     * @param userName
     * @param userPassword
     * @return
     */
    @Override
    public OauthUser findByUserNameAndUserPassword(String userName, String userPassword) {
        return this.toOauthUser(userRepository.findByUserNameAndUserPassword(userName, userPassword));
    }

    /**
     * userName 으로 사용자를 삭제한다.
     * @param userName
     */
    @Override
    public void deleteByUserName(String userName) {
        JPAUserEntity entity = userRepository.findByUserName(userName);
        userRepository.delete(entity);
    }

    /**
     * userName 으로 아바타를 찾는다.
     * @param userName
     * @return
     */
    @Override
    public OauthAvatar getAvatar(String userName) {
        JPAAvatarEntity avatarEntity = avatarRepository.findByUserName(userName);
        OauthAvatar oauthAvatar = new OauthAvatar();
        oauthAvatar.setContentType(avatarEntity.getContentType());
        oauthAvatar.setData(avatarEntity.getData());
        oauthAvatar.setUserName(avatarEntity.getUserName());
        return oauthAvatar;
    }

    /**
     * userName 으로 아바타를 저장한다.
     * @param oauthAvatar
     * @return
     */
    @Override
    public OauthAvatar insertAvatar(OauthAvatar oauthAvatar) {
        JPAAvatarEntity avatarEntity = avatarRepository.findByUserName(oauthAvatar.getUserName());
        if (avatarEntity == null) {
            avatarEntity = new JPAAvatarEntity();
        }
        avatarEntity.setUserName(oauthAvatar.getUserName());
        avatarEntity.setContentType(oauthAvatar.getContentType());
        avatarEntity.setData(oauthAvatar.getData());
        avatarRepository.save(avatarEntity);
        return oauthAvatar;
    }

    /**
     * userName 으로 아바타를 삭제한다.
     * @param userName
     */
    @Override
    public void deleteAvatar(String userName) {
        JPAAvatarEntity avatarEntity = avatarRepository.findByUserName(userName);
        avatarRepository.delete(avatarEntity);
    }

    /**
     * Entity 를 OauthUser 로 변환.
     * @param entity
     * @return
     */
    private OauthUser toOauthUser(JPAUserEntity entity) {
        if (entity == null) {
            return null;
        }
        OauthUser oauthUser = new OauthUser();
        oauthUser.setUserName(entity.getUserName());
        oauthUser.setUserPassword(entity.getUserPassword());
        oauthUser.setMetaData(entity.getMetaData());
        oauthUser.setRegDate(entity.getRegDate());
        oauthUser.setUpdDate(entity.getUpdDate());
        return oauthUser;
    }

    /**
     * OauthUser 를 Entity 로 변환.
     * @param oauthUser
     * @return
     */
    private JPAUserEntity toMyModel(OauthUser oauthUser) {
        if (oauthUser == null) {
            return null;
        }
        JPAUserEntity entity = new JPAUserEntity();
        entity.setUserName(oauthUser.getUserName());
        entity.setUserPassword(oauthUser.getUserPassword());
        entity.setMetaData(oauthUser.getMetaData());
        return entity;
    }
}
