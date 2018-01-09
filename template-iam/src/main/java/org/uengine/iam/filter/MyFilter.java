package org.uengine.iam.filter;

import org.springframework.stereotype.Service;
import org.uengine.iam.oauth2.AccessTokenResponse;
import org.uengine.iam.oauth2.AuthorizeResponse;
import org.uengine.iam.oauth2.OauthFilter;

/**
 * Created by uengine on 2018. 1. 9..
 */
@Service
public class MyFilter implements OauthFilter {
    @Override
    public boolean preTokenIssue(AccessTokenResponse tokenResponse) {
        return true;
    }

    @Override
    public void postTokenIssue(AccessTokenResponse tokenResponse) {
        System.out.println("postTokenIssue!!");
    }

    @Override
    public boolean preAuthorize(AuthorizeResponse authorizeResponse) {
        return true;
    }

    @Override
    public void postAuthorize(AuthorizeResponse authorizeResponse) {
        System.out.println("postAuthorize!!");
    }
}
