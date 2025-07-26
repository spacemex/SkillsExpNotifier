package com.github.spacemex.skillexpnotifier.forge.network.client;

import com.github.spacemex.skillexpnotifier.client.CustomToastComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientNotify {
    private static final CustomToastComponent TOASTS = new CustomToastComponent(Minecraft.getInstance());

    public static void render(GuiGraphics gfx) {
        TOASTS.render(gfx);
    }

    public static CustomToastComponent getToastComponent() {
        return TOASTS;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            render(event.getGuiGraphics());
        }
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ClientNotify.class);
    }
}
