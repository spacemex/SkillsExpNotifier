package com.github.spacemex.skillsexpnotifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.spacemex.skillsexpnotifier.Skillsexpnotifier.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Notifier {
    private static final int MAX_TOASTS = 3;
    private static final long DISPLAY_TIME = 1_000L;
    private static final Map<ResourceLocation, Integer> lastTotals = new HashMap<>();
    private static final List<Long> toastTimestamps = new ArrayList<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;

        ServerPlayer sp = mc.getSingleplayerServer()
                .getPlayerList()
                .getPlayer(mc.player.getUUID());
        if (sp == null) return;

        ToastComponent toasts = mc.getToasts();
        long now = System.currentTimeMillis();

        // attempt purge old entries
        toastTimestamps.removeIf(ts -> now - ts > DISPLAY_TIME);

        SkillsAPI.streamCategories().forEach(cat ->
                cat.getExperience().ifPresent(exp -> {
                    ResourceLocation id = cat.getId();
                    int total = exp.getTotal(sp);
                    int prev = lastTotals.getOrDefault(id, total);

                    if (total > prev) {
                        // only add if there is capacity - Could be better
                        if (toastTimestamps.size() < MAX_TOASTS) {
                            toasts.addToast(new XpToast(cat, total - prev));
                            toastTimestamps.add(now);
                        }
                    }
                    lastTotals.put(id, total);
                })
        );
    }
}
