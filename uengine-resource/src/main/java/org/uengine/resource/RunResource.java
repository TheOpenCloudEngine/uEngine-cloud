package org.uengine.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.MapConfiguration;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.uengine.iam.util.FileUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.resource.templates.MustacheTemplateEngine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * Created by uengine on 2018. 1. 16..
 */
public class RunResource {
    public static void main(String[] args) throws Exception {
        String baseDir = args[0];
        System.out.println(baseDir);

        File file = new File(baseDir + "/config.yml");
        String configYml = new String(Files.readAllBytes(Paths.get(file.getPath())));
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map configMap = yamlReader.readValue(configYml, Map.class);

        File propTemplateFile = new File(baseDir + "/config.properties.yml.template");
        String propTemplateString = new String(Files.readAllBytes(Paths.get(propTemplateFile.getPath())));
        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
        String body = templateEngine.executeTemplateText(propTemplateString, configMap);

        System.out.println(JsonUtils.marshal(configMap));

        //클라우드 패키지 배포 프로퍼티스를 생성한다.
        org.apache.commons.io.FileUtils.writeStringToFile(
                new File(baseDir + "/config.properties.yml"),
                body,
                "UTF-8"
        );

        //etc-host 파일을 생성한다.


        //앤시블 호스트 파일을 생성한다.

    }
}
