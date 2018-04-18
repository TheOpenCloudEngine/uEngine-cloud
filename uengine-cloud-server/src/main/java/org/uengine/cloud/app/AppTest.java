package org.uengine.cloud.app;

import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class AppTest {

    public static void main(String[] args) throws Exception {
        //createTest();
        deleteTest();
    }

    public static void createTest() throws Exception {
        HttpUtils httpUtils = new HttpUtils();

        for (int i = 0; i < 20; i++) {
            String appName = "test" + i;
            AppCreate appCreate = new AppCreate();
            appCreate.setCategoryItemId("springboot");
            appCreate.setCpu(0.4);
            appCreate.setMem(512);
            appCreate.setAppName(appName);
            appCreate.setExternalDevDomain(appName + "-dev.pas-mini.io");
            appCreate.setExternalStgDomain(appName + "-stg.pas-mini.io");
            appCreate.setExternalProdDomain(appName + ".pas-mini.io");

            Map headers = new HashMap();
            headers.put("access_token", "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ1ZW5naW5lLWNsb3VkLWlhbSIsImNvbnRleHQiOnsiY2xpZW50S2V5IjoibXktY2xpZW50LWtleSIsInNjb3BlcyI6WyJjbG91ZC1zZXJ2ZXIiXSwidHlwZSI6InVzZXIiLCJ1c2VyTmFtZSI6Imp5amFuZ0B1ZW5naW5lLm9yZyIsInVzZXIiOnsidXNlck5hbWUiOiJqeWphbmdAdWVuZ2luZS5vcmciLCJtZXRhRGF0YSI6eyJhY2wiOiJhZG1pbiIsImVtYWlsIjoianlqYW5nQHVlbmdpbmUub3JnIiwibmFtZSI6IuyepeynhOyYgSIsImdpdGxhYi1pZCI6NSwic2NvcGVzIjpbImNsb3VkLXNlcnZlciIsImJwbSIsImNvdXJzZS1tZ210Il0sImdpdGxhYlRva2VuIjoiUDdZeU1rSlAyVzRmdVArMzN6cTlsbXBZUkI0NW1nOVd4MGJKYXhCeDg1QT0ifSwicmVnRGF0ZSI6MTUyMjA0MDU3NzI3MSwidXBkRGF0ZSI6MTUyMjMwMzUzMzY4OX0sInJlZnJlc2hUb2tlbiI6IjQxYjdmODk5LTJhNjctNDJmMi1iNTYxLTBlNTJjMDMxOGM5OSJ9LCJjbGFpbSI6e30sImV4cCI6MTUyMzQyNjU4MSwiaWF0IjoxNTIzNDE5MzgxfQ.XQcHPsoQYme2YZ8JZuq6AZnDYOy_blia4v4juwyH_GI");
            headers.put("Content-Type", "application/json");

            httpUtils.makeRequest("POST", "http://localhost:8080/app",
                    JsonUtils.marshal(appCreate), headers);
        }
    }

    public static void deleteTest() throws Exception {
        HttpUtils httpUtils = new HttpUtils();

        for (int i = 0; i < 20; i++) {
            String appName = "test" + i;
            Map headers = new HashMap();
            headers.put("access_token", "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ1ZW5naW5lLWNsb3VkLWlhbSIsImNvbnRleHQiOnsiY2xpZW50S2V5IjoibXktY2xpZW50LWtleSIsInNjb3BlcyI6WyJjbG91ZC1zZXJ2ZXIiXSwidHlwZSI6InVzZXIiLCJ1c2VyTmFtZSI6Imp5amFuZ0B1ZW5naW5lLm9yZyIsInVzZXIiOnsidXNlck5hbWUiOiJqeWphbmdAdWVuZ2luZS5vcmciLCJtZXRhRGF0YSI6eyJhY2wiOiJhZG1pbiIsImVtYWlsIjoianlqYW5nQHVlbmdpbmUub3JnIiwibmFtZSI6IuyepeynhOyYgSIsImdpdGxhYi1pZCI6NSwic2NvcGVzIjpbImNsb3VkLXNlcnZlciIsImJwbSIsImNvdXJzZS1tZ210Il0sImdpdGxhYlRva2VuIjoiUDdZeU1rSlAyVzRmdVArMzN6cTlsbXBZUkI0NW1nOVd4MGJKYXhCeDg1QT0ifSwicmVnRGF0ZSI6MTUyMjA0MDU3NzI3MSwidXBkRGF0ZSI6MTUyMjMwMzUzMzY4OX0sInJlZnJlc2hUb2tlbiI6IjQxYjdmODk5LTJhNjctNDJmMi1iNTYxLTBlNTJjMDMxOGM5OSJ9LCJjbGFpbSI6e30sImV4cCI6MTUyMzQyNjU4MSwiaWF0IjoxNTIzNDE5MzgxfQ.XQcHPsoQYme2YZ8JZuq6AZnDYOy_blia4v4juwyH_GI");
            headers.put("Content-Type", "application/json");

            httpUtils.makeRequest("DELETE", "http://localhost:8080/app/" + appName,
                    null, headers);
        }
    }
}
