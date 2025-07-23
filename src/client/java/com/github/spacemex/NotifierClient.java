package com.github.spacemex;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public class NotifierClient implements ClientModInitializer {
    private static final CustomToastComponent TOASTS = new CustomToastComponent(MinecraftClient.getInstance());

    public static CustomToastComponent getToastComponent() {
        return TOASTS;
    }

    @Override
    public void onInitializeClient() {
        // Register render overlay hook
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            TOASTS.render(graphics);
        });

        // Optionally listen for client ticks, if needed
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Tick-related logic (if needed)
        });
    }
}
