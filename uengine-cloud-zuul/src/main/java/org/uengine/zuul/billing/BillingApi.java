package org.uengine.zuul.billing;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillingApi {

    private Logger logger = LoggerFactory.getLogger(BillingApi.class);

    @Autowired
    private BillingConfig billingConfig;

    public Map getAccountById(String accountId) {
        String method = "GET";
        String path = "/rest/v1/accounts/" + accountId;

        Map headers = new HashMap();
        try {
            HttpResponse httpResponse = this.apiRequest(method, path, null, headers);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                return JsonUtils.marshal(result);
            } else {
                logger.error("Not found account");
                return null;
            }
        } catch (IOException ex) {
            logger.error("Failed to request account");
            return null;
        }
    }

    public Map getAccountByExternalKey(String userName) {
        String method = "GET";
        String path = "/rest/v1/accounts?externalKey=" + userName;

        Map headers = new HashMap();
        try {
            HttpResponse httpResponse = this.apiRequest(method, path, null, headers);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                return JsonUtils.marshal(result);
            } else {
                logger.error("Not found account");
                return null;
            }
        } catch (IOException ex) {
            logger.error("Failed to request account");
            return null;
        }
    }

    public List<Map> getAccountBundles(String accountId) {
        String method = "GET";
        String path = "/rest/v1/accounts/" + accountId + "/bundles?audit=NONE";

        Map headers = new HashMap();
        try {
            HttpResponse httpResponse = this.apiRequest(method, path, null, headers);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                return JsonUtils.unmarshalToList(result);
            } else {
                logger.error("Not found account bundles");
                return null;
            }
        } catch (IOException ex) {
            logger.error("Failed to request account bundles");
            return null;
        }
    }

    public HttpResponse apiRequest(String method, String path, String data, Map headers) throws IOException {
        Map requiredHeaders = new HashMap();
        requiredHeaders.put("Authorization", "Basic " + billingConfig.getAuthentication());
        requiredHeaders.put("Content-Type", "application/json");
        requiredHeaders.put("Accept", "application/json");
        requiredHeaders.put("X-organization-id", billingConfig.getOrganization());
        requiredHeaders.putAll(headers);

        String url = billingConfig.getUrl() + path;
        HttpUtils httpUtils = new HttpUtils();
        HttpResponse httpResponse = httpUtils.makeRequest(method, url, data, requiredHeaders);
        return httpResponse;
    }
}
