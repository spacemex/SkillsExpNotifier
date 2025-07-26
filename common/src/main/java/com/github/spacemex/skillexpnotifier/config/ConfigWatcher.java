package com.github.spacemex.skillexpnotifier.config;

import com.github.spacemex.skillexpnotifier.Skillexpnotifier;
import com.github.spacemex.yml.YamlConfigUtil;
import dev.architectury.event.events.client.ClientTickEvent;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class ConfigWatcher {
    private static long lastModified = -1;
    private static YamlConfigUtil config;

    public static void  init(){
        reloadConfig();
        ClientTickEvent.CLIENT_POST.register(client -> tick());
    }

    private static void tick(){
        if (!getBoolean("Client-Settings.File-Watcher",true)) return;
        try{
            long currentModified = Files.getLastModifiedTime(Skillexpnotifier.CONFIG_FILE).toMillis();
            if (currentModified > lastModified){
                lastModified = currentModified;
                reloadConfig();
                Skillexpnotifier.LOGGER.info("[File Watcher] Config Reloaded");
            }
        }catch (IOException e){
            Skillexpnotifier.LOGGER.error("[File Watcher] Failed to read file modified time",e);
        }
    }

    private static void reloadConfig(){
        try(FileInputStream fis = new FileInputStream(Skillexpnotifier.CONFIG_FILE.toFile())){
            Yaml yaml = new Yaml();
            Map<String,Object> data = yaml.load(fis);
            config = new YamlConfigUtil(data);
            lastModified = Files.getLastModifiedTime(Skillexpnotifier.CONFIG_FILE).toMillis();
        }catch (IOException e){
            Skillexpnotifier.LOGGER.error("[File Watcher] Failed to load config",e);
        }
    }

    private static boolean getBoolean(String key, boolean defaultValue){
        return config != null ? config.getBoolean(key, defaultValue) : defaultValue;
    }
}
