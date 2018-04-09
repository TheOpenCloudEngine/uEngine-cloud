package org.uengine.cloud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.gitlab4j.api.GitLabApi;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by uengine on 2017. 11. 20..
 */
public class GitlabTest {
    public static void main(String[] args) {
        try {
            GitLabApi gitLabApi = new GitLabApi("http://gitlab.uengine.io", "h3StqSVtxwyLk6rN-BgF");
//            Project project = gitLabApi.getProjectApi().getProject(30);
//            if(project != null){
//
//            }

//            Map headers = new HashMap();
//            headers.put("Content-Type", "application/json");
//            headers.put("PRIVATE-TOKEN", "h3StqSVtxwyLk6rN-BgF");
//            HttpResponse response = new HttpUtils().makeRequest("GET",
//                    "http://gitlab.uengine.io/api/v4/runners/all",
//                    null,
//                    headers
//            );
//            HttpEntity entity = response.getEntity();
//            String json = EntityUtils.toString(entity);
//            System.out.println(json);

            InputStream inputStream = gitLabApi.getRepositoryFileApi().getRawFile(6, "master", "dcos-apps.yml");
            String yaml = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
//            String url = "http://config.uengine.io/dcos-apps.yml";
//            HttpResponse httpResponse = new HttpUtils().makeRequest("GET", url, null, new HashMap<>());
//            HttpEntity entity = httpResponse.getEntity();
//            String yaml = EntityUtils.toString(entity);

            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(yaml, Object.class);
            String s = yamlReader.writeValueAsString(yaml);

//            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
//            Object obj = yamlReader.readValue(yaml, Object.class);
            //Map map = JsonUtils.marshal(json);


            System.out.println("end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
