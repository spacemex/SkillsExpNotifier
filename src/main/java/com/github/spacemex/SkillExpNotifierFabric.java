package com.github.spacemex;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkillExpNotifierFabric implements ModInitializer {
	public static final String MOD_ID = "skillexpnotifier";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier XP_GAIN_PACKET_ID = new Identifier(MOD_ID, "network");
    public static final String PROTOCOL_VERSION = "1";
    public static final Identifier CHANNEL_ID = new Identifier(MOD_ID, "network");

    @Override
    public void onInitialize() {
        LOGGER.info("SkillExpNotifier Fabric initializing…");
        NotifierServer.register(); // your server-side logic registration
        registerPackets();
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(XP_GAIN_PACKET_ID, (server, player, handler, buf, responseSender) -> {
        });
    }

    public static void sendXpGainPacket(ServerPlayerEntity player, XpGainPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        XpGainPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, XP_GAIN_PACKET_ID, buf);
    }
}