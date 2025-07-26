package com.github.spacemex.skillexpnotifier.fabric.network.server;

import com.github.spacemex.skillexpnotifier.fabric.SkillexpnotifierFabric;
import com.github.spacemex.skillexpnotifier.network.XpGainPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerNotify {
    private static final Map<UUID, Map<ResourceLocation, Integer>> lastTotals = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ServerNotify::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            var playerMap = lastTotals.computeIfAbsent(uuid, __ -> new HashMap<>());

            SkillsAPI.streamCategories().forEach(cat ->
                    cat.getExperience().ifPresent(exp -> {
                        ResourceLocation id = ResourceLocation.tryParse(cat.getId().toString());
                        int total = exp.getTotal(player);
                        int prev = playerMap.getOrDefault(id, total);
                        if (total > prev) {
                            int delta = total - prev;
                            SkillexpnotifierFabric.sendXpGainPacket(player, new XpGainPacket(id, delta));
                        }
                        playerMap.put(id, total);
                    })
            );
        }
    }
}
