package com.github.spacemex;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ClientLoginHandler {
    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            SearchForSkills.scan();
        });
    }
}
