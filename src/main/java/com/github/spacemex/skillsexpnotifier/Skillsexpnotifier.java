package com.github.spacemex.skillsexpnotifier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod(Skillsexpnotifier.MODID)
public class Skillsexpnotifier {
    public static final String MODID = "skillsexpnotifier";
    public Skillsexpnotifier() {
        MinecraftForge.EVENT_BUS.register(this);
        Path configDir = FMLPaths.CONFIGDIR.get();
        EntryRegistry.loadFromFile(configDir);
    }
}