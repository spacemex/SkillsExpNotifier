package com.github.spacemex.skillexpnotifier.forge;

import com.github.spacemex.skillexpnotifier.Skillexpnotifier;
import com.github.spacemex.skillexpnotifier.client.EntryRegistry;
import com.github.spacemex.skillexpnotifier.discovery.SearchForSkills;
import com.github.spacemex.skillexpnotifier.forge.network.client.ClientNotify;
import com.github.spacemex.skillexpnotifier.network.XpGainPacket;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(Skillexpnotifier.MOD_ID)
public final class SkillexpnotifierForge {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Skillexpnotifier.MOD_ID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public SkillexpnotifierForge(FMLJavaModLoadingContext context) {
        EventBuses.registerModEventBus(Skillexpnotifier.MOD_ID, context.getModEventBus());
        Skillexpnotifier.init();

        context.getModEventBus().addListener(this::onCommonSetup);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            context.getModEventBus().addListener(this::clientSetup);
        });
    }
    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        EntryRegistry.loadFromFile();
        SearchForSkills.scan();
        ClientNotify.register();
        Skillexpnotifier.LOGGER.info("Client setup finished.");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        Skillexpnotifier.LOGGER.info("SkillExpNotifier common setup, registering packets…");
        CHANNEL.registerMessage(
                0,
                XpGainPacket.class,
                XpGainPacket::encode,
                XpGainPacket::decode,
                (msg, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    ctx.setPacketHandled(true);
                    ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
                        com.github.spacemex.skillexpnotifier.forge.ToastPacketHandler.onXpGain(msg);
                    }));
                }
        );
        Skillexpnotifier.LOGGER.info("Packet registration complete.");
    }
}
