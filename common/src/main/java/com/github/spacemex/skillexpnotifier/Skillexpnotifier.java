package com.github.spacemex.skillexpnotifier;

import com.github.spacemex.skillexpnotifier.config.Config;
import com.github.spacemex.skillexpnotifier.config.ConfigWatcher;
import dev.architectury.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class Skillexpnotifier {
    public static final String MOD_ID = "skillexpnotifier";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path GAME_DIR = Platform.getGameFolder();
    public static final Path CONFIG_DIR = Platform.getConfigFolder().resolve("SkillExpNotifier");
    public static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.yml");
    public static final Path SKILLS_DIR = CONFIG_DIR.resolve("IconMapping.json");

    public static void init() {
        Config.generateConfig();
        ConfigWatcher.init();
    }
}
