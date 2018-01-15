package org.uengine.cloud.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.TreeItem;
import org.gitlab4j.api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.AppCreate;
import org.uengine.cloud.app.DcosApi;
import org.uengine.cloud.app.GitlabExtentApi;
import org.uengine.cloud.app.HookController;
import org.uengine.cloud.scheduler.CronTable;
import org.uengine.cloud.scheduler.JobScheduler;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class CatalogService {
    @Autowired
    private Environment environment;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;


    public CategoryItem getCategoryItemWithFiles(String categoryItemId) throws Exception {
        List<Project> projects = gitLabApi.getProjectApi().getProjects("template-" + categoryItemId);
        if (projects.isEmpty()) {
            throw new Exception("Not found template project, template-" + categoryItemId);
        }
        Project project = projects.get(0);
        String catalogString = gitlabExtentApi.getRepositoryFile(project.getId(), "master", "template/catalog.json");
        Map unmarshal = JsonUtils.unmarshal(catalogString);
        CategoryItem categoryItem = JsonUtils.convertValue(unmarshal, CategoryItem.class);

        categoryItem.setProjectId(project.getId());
        String[] files = new String[]{
                "ci-deploy-dev.json",
                "ci-deploy-production.json",
                "ci-deploy-staging.json",
                "config.yml",
                "config-dev.yml",
                "config-prod.yml",
                "config-stg.yml",
        };
        for (String file : files) {
            String content = gitlabExtentApi.getRepositoryFile(project.getId(), "master", "template/deploy/" + file);
            switch (file) {
                case "ci-deploy-dev.json":
                    categoryItem.setDeployDev(content);
                    break;
                case "ci-deploy-production.json":
                    categoryItem.setDeployProd(content);
                    break;
                case "ci-deploy-staging.json":
                    categoryItem.setDeployStg(content);
                    break;
                case "config.json":
                    categoryItem.setConfig(content);
                    break;
                case "config-dev.json":
                    categoryItem.setConfigDev(content);
                    break;
                case "config-prod.json":
                    categoryItem.setConfigProd(content);
                    break;
                case "config-stg.json":
                    categoryItem.setConfigStg(content);
                    break;
            }
        }

        //매핑을 실제 파일로 교체
        List<FileMapping> mappings = categoryItem.getMappings();
        if (!mappings.isEmpty()) {
            for (FileMapping mapping : mappings) {
                String file = mapping.getFile();
                String fileContent = gitlabExtentApi.getRepositoryFile(project.getId(), "master", "template/file/" + file);
                mapping.setFile(fileContent);
            }
        }

        return categoryItem;
    }

    public List<Category> getCategories() throws Exception {
        List<Category> categories = this.getCategoryList();
        List<Project> projects = this.getTemplateProjects();

        for (Project project : projects) {

            //카테고리 아이템
            List<TreeItem> tree = gitLabApi.getRepositoryApi().getTree(project.getId(), "template", "master");
            if (!tree.isEmpty()) {
                CategoryItem categoryItem = null;
                String logoSrc = null;
                for (TreeItem treeItem : tree) {
                    //카달로그 파일 얻기
                    if (treeItem.getName().equals("catalog.json")) {
                        try {
                            String catalogString = gitlabExtentApi.getRepositoryFile(project.getId(), "master", "template/catalog.json");
                            Map marshal = JsonUtils.marshal(catalogString);
                            categoryItem = JsonUtils.convertValue(marshal, CategoryItem.class);
                        } catch (Exception ex) {

                        }
                    }
                    //로고 파일 얻기
                    if (treeItem.getName().startsWith("logo")) {
                        logoSrc = project.getWebUrl() + "/raw/master/template/" + treeItem.getName();
                    }
                }
                //카달로그 파일이 있을 경우
                if (categoryItem != null) {
                    categoryItem.setProjectId(project.getId());
                    categoryItem.setProjectUrl(project.getWebUrl());
                    categoryItem.setLogoSrc(logoSrc);

                    //카달로그의 카테고리 찾기
                    String categoryId = categoryItem.getCategory();
                    for (Category category : categories) {
                        if (category.getId().equals(categoryId)) {
                            categoryItem.setType(category.getType());
                            category.getItems().add(categoryItem);
                        }
                    }
                }
            }
        }
        return categories;
    }

    private List<Category> getCategoryList() throws Exception {
        List<Category> list = new ArrayList<>();

        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String filePath = "template/category.json";
        String content = gitlabExtentApi.getRepositoryFile(repoId, "master", filePath);
        List categories = JsonUtils.unmarshalToList(content);
        for (Object category : categories) {
            list.add(JsonUtils.convertValue(category, Category.class));
        }
        return list;
    }

    public List<Project> getTemplateProjects() throws Exception {
        return gitLabApi.getProjectApi().getProjects("template-");
    }
}
