package com.github.spacemex;

import com.github.spacemex.yml.YamlConfigUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ConfigWatcher {
    private static final Path CONFIG_PATH = SkillExpNotifierFabricClient.CONFIG_PATH;
    private static long lastModified = -1;
    private static YamlConfigUtil config;

    public static void initWatcher() {
        reloadConfig(); // load once at startup
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    private static void tick() {
        if (!getBoolean("Client-Settings.File-Watcher", true)) return;

        try {
            long currentModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
            if (currentModified > lastModified) {
                lastModified = currentModified;
                reloadConfig();
                SkillExpNotifierFabric.LOGGER.info("[File Watcher] Reloaded YAML config");
            }
        } catch (IOException e) {
            SkillExpNotifierFabric.LOGGER.error("Failed to read file modified time", e);
        }
    }

    private static void reloadConfig() {
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH.toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(fis);
            config = new YamlConfigUtil(data);
            lastModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
        } catch (IOException e) {
            SkillExpNotifierFabric.LOGGER.error("[File Watcher] Failed to load config file", e);
        }
    }

    private static boolean getBoolean(String key, boolean fallback) {
        return config != null ? config.getBoolean(key, fallback) : fallback;
    }
}
