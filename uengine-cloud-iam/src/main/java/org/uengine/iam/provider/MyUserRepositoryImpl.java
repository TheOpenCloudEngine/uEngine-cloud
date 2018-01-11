package org.uengine.iam.provider;


import org.apache.commons.lang.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.uengine.iam.oauthuser.OauthAvatar;
import org.uengine.iam.oauthuser.OauthUser;
import org.uengine.iam.oauthuser.OauthUserPage;
import org.uengine.iam.oauthuser.OauthUserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyUserRepositoryImpl implements OauthUserRepository {

    @Autowired
    private JPAUserRepository userRepository;

    @Autowired
    private JPAAvatarRepository avatarRepository;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    /**
     * 사용자를 생성한다.
     *
     * @param oauthUser
     * @return
     */
    @Override
    public OauthUser insert(OauthUser oauthUser) {
        //메타데이터에 acl 이 있는지 확인
        if (!oauthUser.getMetaData().containsKey("acl")) {
            throw new RuntimeException("acl is required.");
        }

        //깃랩은 패스워드가 8자 이하면 불가.
        if (oauthUser.getUserPassword().length() < 8) {
            throw new RuntimeException("password is too short. more than 8 characters required.");
        }


        try {

            //==========깃랩 유저 생성====================
            //userId == 이메일 주소.
            //깃랩에 해당 이메일이나 유저네임을 사용하는 유저가 있는지 체크.

            User gitlabUser = null;
            try {
                List<User> users = gitLabApi.getUserApi().findUsers(oauthUser.getUserName());
                if (!users.isEmpty()) {
                    for (User user : users) {
                        if (oauthUser.getUserName().equals(user.getEmail()) || oauthUser.getUserName().equals(user.getUsername())) {
                            gitlabUser = user;
                        }
                    }
                }
            } catch (Exception ex) {
            }

            //깃랩에서 해당 유저가 없다면, 깃랩에 아이디를 생성한다.
            //깃랩 사용자가 없다면 생성.
            if (gitlabUser == null) {
                String gitlabUsername = oauthUser.getUserName().split("@")[0];
                String gitlabName = gitlabUsername;
                if (oauthUser.getMetaData().containsKey("name")) {
                    gitlabName = oauthUser.getMetaData().get("name").toString();
                }

                Map userMap = new HashMap();
                userMap.put("name", gitlabName);
                userMap.put("username", gitlabUsername);
                userMap.put("email", oauthUser.getUserName());
                userMap.put("password", oauthUser.getUserPassword());
                userMap.put("skip_confirmation", true);
                Map created = gitlabExtentApi.createUser(userMap);
                int gitlabId = (int) created.get("id");
                oauthUser.getMetaData().put("gitlab-id", gitlabId);

            }
            //깃랫 사용자가 있다면, 바로 그 아이디를 사용
            else {
                oauthUser.getMetaData().put("gitlab-id", gitlabUser.getId());
            }


            JPAUserEntity entity = this.toMyModel(oauthUser);
            return this.toOauthUser(userRepository.save(entity));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 사용자를 업데이트한다.
     *
     * @param oauthUser
     * @return
     */
    @Override
    public OauthUser update(OauthUser oauthUser) {
        JPAUserEntity toUpdate = userRepository.findByUserName(oauthUser.getUserName());
        toUpdate.setUserPassword(oauthUser.getUserPassword());
        toUpdate.setMetaData(oauthUser.getMetaData());
        return this.toOauthUser(userRepository.save(toUpdate));
    }

    /**
     * userName 으로 사용자를 찾는다.
     *
     * @param userName
     * @return
     */
    @Override
    public OauthUser findByUserName(String userName) {
        return this.toOauthUser(userRepository.findByUserName(userName));
    }


    /**
     * searchKey 로 userName 을 Like 검색한다.
     *
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
     *
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
     *
     * @param userName
     */
    @Override
    public void deleteByUserName(String userName) {
        JPAUserEntity entity = userRepository.findByUserName(userName);
        userRepository.delete(entity);
    }

    /**
     * userName 으로 아바타를 찾는다.
     *
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
     *
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
     *
     * @param userName
     */
    @Override
    public void deleteAvatar(String userName) {
        JPAAvatarEntity avatarEntity = avatarRepository.findByUserName(userName);
        avatarRepository.delete(avatarEntity);
    }

    /**
     * Entity 를 OauthUser 로 변환.
     *
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
     *
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
