package com.github.spacemex;

import com.github.spacemex.yml.YamlConfigUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ConfigHelper {
    private static final File yamlFile = SkillExpNotifierFabricClient.CONFIG_PATH.toFile();
    private final YamlConfigUtil util;

    public ConfigHelper() {
        Yaml yaml = new Yaml();
        Map<String, Object> data;
        try (FileReader reader = new FileReader(yamlFile)) {
            data = yaml.load(reader);
        }catch (IOException e){
            throw new RuntimeException("Failed to load YAML config",e);
        }

        util = new YamlConfigUtil(data);
    }

    public static YamlConfigUtil getConfigUtil() {
        return new ConfigHelper().util;
    }
}
