package com.github.spacemex.skillexpnotifier.fabric.network.client;

import com.github.spacemex.skillexpnotifier.client.CustomToastComponent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class ClientNotify implements ClientModInitializer {
    private static final CustomToastComponent TOASTS = new CustomToastComponent(Minecraft.getInstance());

    public static CustomToastComponent getToastComponent() {
        return TOASTS;
    }

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            TOASTS.render(graphics);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
        });
    }
}
