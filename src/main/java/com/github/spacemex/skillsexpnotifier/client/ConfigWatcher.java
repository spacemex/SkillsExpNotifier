package com.github.spacemex.skillsexpnotifier.client;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.github.spacemex.skillsexpnotifier.Config;
import com.github.spacemex.skillsexpnotifier.Skillsexpnotifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
@Mod.EventBusSubscriber(modid= Skillsexpnotifier.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
public class ConfigWatcher {
    private static final Path CONFIG = FMLPaths.CONFIGDIR.get().resolve(Config.CONFIG_PATH);
    private static long lastModified = -1;

    public static void initWatcher(){
        try {
            lastModified = Files.getLastModifiedTime(CONFIG).toMillis();
            Skillsexpnotifier.LOGGER.info("[File Watcher] Config loaded, last modified: {}",lastModified);

        }catch (IOException e){
            Skillsexpnotifier.LOGGER.error("[File Watcher]Could not read Config timeStamp",e);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (!Config.ENABLE_FILE_WATCHER.get()) return;
        if (event.phase != TickEvent.Phase.END) return;
        try {
            long lm = Files.getLastModifiedTime(CONFIG).toMillis();
            if (lm > lastModified) {
                lastModified = lm;
                reloadConfig();
                Skillsexpnotifier.LOGGER.info("[File Watcher] Config changed, reloading...");
            }
        } catch (IOException e) {
            Skillsexpnotifier.LOGGER.error("Could not read Config timeStamp", e);
        }
    }

    private static void reloadConfig(){
        if (!Config.ENABLE_FILE_WATCHER.get()) return;
        CommentedFileConfig cf = CommentedFileConfig.builder(CONFIG)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
        cf.load();
        Config.SPEC.setConfig(cf);
        Skillsexpnotifier.LOGGER.info("[File Watcher] Config reloaded.");
    }
}
