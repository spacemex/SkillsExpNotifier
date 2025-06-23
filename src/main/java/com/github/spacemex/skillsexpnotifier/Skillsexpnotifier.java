package com.github.spacemex.skillsexpnotifier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Mod(Skillsexpnotifier.MODID)
public class Skillsexpnotifier {
    public static final String MODID = "skillsexpnotifier";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public Skillsexpnotifier(FMLJavaModLoadingContext context) {
        MinecraftForge.EVENT_BUS.register(this);
        context.getModEventBus().addListener(this::onClientSetup);
        context.getModEventBus().addListener(this::onCommonSetup);
        //MinecraftForge.EVENT_BUS.register(ConfigWatcher.class);
        Config.register(context);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        Skillsexpnotifier.LOGGER.info("Mods are loaded, now scanning for skills…");
        Path configDir = FMLPaths.CONFIGDIR.get();
        EntryRegistry.loadFromFile(configDir);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        ConfigWatcher.initWatcher();
        Skillsexpnotifier.LOGGER.info("SkillExpNotifier initialized.");
    }
}