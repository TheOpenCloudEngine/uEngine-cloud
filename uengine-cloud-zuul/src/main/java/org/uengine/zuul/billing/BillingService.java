package org.uengine.zuul.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.zuul.tenant.TokenContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BillingService {
    @Autowired
    BillingRedisRepository billingRedisRepository;

    @Autowired
    BillingApi billingApi;

    @Autowired
    BillingConfig billingConfig;


    public void updateUserSubscriptionsByAccountId(String accountId) {
        Map account = billingApi.getAccountById(accountId);
        if (account == null) {
            return;
        }
        String userName = account.get("externalKey").toString();
        UserSubscriptions userSubscriptions = new UserSubscriptions();
        userSubscriptions.setAccountId(accountId);

        List<UserSubscriptions.Subscription> list = this.getAccountActiveSubscriptions(accountId);
        userSubscriptions.setSubscriptions(list);

        billingRedisRepository.saveUserSubscriptions(userName, userSubscriptions);
    }

    /**
     * IAM 유저네임으로 현재 사용자가 구독중인 리스트를 가져온다.
     *
     * @param userName
     * @return
     */
    public UserSubscriptions getUserSubscriptionsByUserName(String userName) {
        UserSubscriptions userSubscriptions = billingRedisRepository.getUserSubscriptions(userName);

        if (userSubscriptions == null) {
            userSubscriptions = new UserSubscriptions();
            Map account = billingApi.getAccountByExternalKey(userName);

            //if account exist? get subscriptions by accountId
            if (account != null) {
                String accountId = account.get("accountId").toString();
                userSubscriptions.setAccountId(accountId);

                List<UserSubscriptions.Subscription> list = this.getAccountActiveSubscriptions(accountId);
                userSubscriptions.setSubscriptions(list);
            }
            //if account not exist? save empty cache
            else {
                userSubscriptions = new UserSubscriptions();
            }
            //finally, save userSubscriptions to redis.
            billingRedisRepository.saveUserSubscriptions(userName, userSubscriptions);
        }

        return userSubscriptions;
    }

    /**
     * 빌링 어카운트 아이디로 현재 정상 이용중인 구독 리스트를 반환한다.
     *
     * @param accountId
     * @return
     */
    public List<UserSubscriptions.Subscription> getAccountActiveSubscriptions(String accountId) {
        List<Map> bundles = billingApi.getAccountBundles(accountId);
        if (bundles == null) {
            return new ArrayList<>();
        } else {
            ArrayList<UserSubscriptions.Subscription> list = new ArrayList<>();
            for (int i = 0; i < bundles.size(); i++) {
                Map bundle = bundles.get(i);
                List<Map> subscriptions = (List<Map>) bundle.get("subscriptions");
                for (Map subscription : subscriptions) {
                    if ("ACTIVE".equals(subscription.get("state"))) {
                        UserSubscriptions.Subscription record = new UserSubscriptions.Subscription();
                        record.setId(subscription.get("subscriptionId").toString());
                        record.setPlan(subscription.get("planName").toString());

                        String product = subscription.get("planName").toString().substring(0, 14);
                        record.setProduct(product);
                        list.add(record);
                    }
                }
            }
            return list;
        }
    }
}
