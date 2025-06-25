package com.github.spacemex.skillsexpnotifier;

import com.github.spacemex.skillsexpnotifier.client.ConfigWatcher;
import com.github.spacemex.skillsexpnotifier.client.EntryRegistry;
import com.github.spacemex.skillsexpnotifier.client.NotifierClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Mod(Skillsexpnotifier.MODID)
public class Skillsexpnotifier {
    public static final String MODID = "skillsexpnotifier";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MODID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public Skillsexpnotifier(FMLJavaModLoadingContext ctx) {
        ctx.getModEventBus().addListener(this::onCommonSetup);
        ctx.getModEventBus().addListener(this::onClientSetup);
        Config.register(ctx);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("SkillExpNotifier common setup, registering packets…");
        CHANNEL.registerMessage(
                0,
                XpGainPacket.class,
                XpGainPacket::encode,
                XpGainPacket::decode,
                (msg, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    ctx.setPacketHandled(true);
                    ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
                        com.github.spacemex.skillsexpnotifier.client.ToastPacketHandler.onXpGain(msg);
                    }));
                }
        );
        LOGGER.info("Packet registration complete.");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Client setup: loading icon mappings and hooking toasts…");
        Path configDir = FMLPaths.CONFIGDIR.get();
        EntryRegistry.loadFromFile(configDir);

        NotifierClient.register();
        ConfigWatcher.initWatcher();

        LOGGER.info("Client setup finished.");
    }
}
