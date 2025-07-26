package com.github.spacemex.skillexpnotifier.fabric;

import com.github.spacemex.skillexpnotifier.Skillexpnotifier;
import com.github.spacemex.skillexpnotifier.fabric.network.server.ServerNotify;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class SkillexpnotifierFabric implements ModInitializer {
    public static final ResourceLocation XP_GAIN_PACKET_ID = new ResourceLocation(Skillexpnotifier.MOD_ID + "network");
    @Override
    public void onInitialize() {
        Skillexpnotifier.init();
        ServerNotify.register();
        registerPackets();
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(XP_GAIN_PACKET_ID, (server, player,
                                                                        handler,
                                                                        buf, responseSender) -> {
        });
    }

    public static void sendXpGainPacket(ServerPlayer player, com.github.spacemex.skillexpnotifier.network.XpGainPacket packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        com.github.spacemex.skillexpnotifier.network.XpGainPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, XP_GAIN_PACKET_ID, buf);
    }
}
