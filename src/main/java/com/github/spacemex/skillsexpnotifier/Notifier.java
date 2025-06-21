package com.github.spacemex.skillsexpnotifier;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.HashMap;
import java.util.Map;

import static com.github.spacemex.skillsexpnotifier.Skillsexpnotifier.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class Notifier {
    private static final Map<ResourceLocation, Integer> lastTotals = new HashMap<>();
    private static CustomToastComponent customToasts;
    private static boolean scanned = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;

        // lazy init and optional scanning
        if (customToasts == null) {
            customToasts = new CustomToastComponent(mc);
            if (Config.AUTOREGISTER_DATA.get() && !scanned) {
                SearchForSkills.scan();
                scanned = true;
            }
        }

        ServerPlayer sp = mc.getSingleplayerServer()
                .getPlayerList()
                .getPlayer(mc.player.getUUID());
        if (sp == null) return;

        SkillsAPI.streamCategories().forEach(category ->
                category.getExperience().ifPresent(exp -> {
                    ResourceLocation id = category.getId();
                    int total = exp.getTotal(sp);
                    int prev  = lastTotals.getOrDefault(id, total);

                    if (total > prev) {
                        int gained = total - prev;
                        // try to find an existing toast for this category
                        XpToast existing = customToasts.getToast(XpToast.class, id);
                        if (existing != null) {
                            // just bump its counter
                            existing.addGained(gained);
                        } else {
                            // create a new one
                            customToasts.addToast(new XpToast(category, gained));
                        }
                    }
                    lastTotals.put(id, total);
                })
        );
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        if (customToasts != null) {
            customToasts.render(event.getGuiGraphics());
        }
    }
}