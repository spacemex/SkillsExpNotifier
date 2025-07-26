package com.github.spacemex.skillexpnotifier.config;

import com.github.spacemex.skillexpnotifier.Skillexpnotifier;
import com.github.spacemex.yml.YamlConfigUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ConfigReader {
    private static final File yamlFile = Skillexpnotifier.CONFIG_FILE.toFile();
    private final YamlConfigUtil util;
    public ConfigReader() {
        Yaml yaml = new Yaml();
        Map<String,Object> data;
        try(FileReader reader = new FileReader(yamlFile)){
            data = yaml.load(reader);
        }catch (IOException e){
            throw new RuntimeException("Failed to load config file",e);
        }
        util = new YamlConfigUtil(data);
    }
    public YamlConfigUtil getConfig() {
        return util;
    }
}
