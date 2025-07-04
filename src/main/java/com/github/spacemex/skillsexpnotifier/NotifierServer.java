package com.github.spacemex.skillsexpnotifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Skillsexpnotifier.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NotifierServer {
    private static final Map<UUID, Map<ResourceLocation, Integer>> lastTotals = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            var playerMap = lastTotals.computeIfAbsent(uuid, __ -> new HashMap<>());

            SkillsAPI.streamCategories().forEach(cat ->
                    cat.getExperience().ifPresent(exp -> {
                        ResourceLocation id = cat.getId();
                        int total = exp.getTotal(player);
                        int prev  = playerMap.getOrDefault(id, total);
                        if (total > prev) {
                            int delta = total - prev;
                            // send packet to that one player
                            Skillsexpnotifier.CHANNEL.send(
                                    PacketDistributor.PLAYER.with(() -> player),
                                    new XpGainPacket(id, delta)
                            );
                        }
                        playerMap.put(id, total);
                    })
            );
        }
    }
}