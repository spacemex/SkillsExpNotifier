package com.github.spacemex.skillexpnotifier.forge;

import com.github.spacemex.skillexpnotifier.client.XpToast;
import com.github.spacemex.skillexpnotifier.forge.network.client.ClientNotify;
import com.github.spacemex.skillexpnotifier.network.XpGainPacket;
import net.minecraft.resources.ResourceLocation;

public class ToastPacketHandler {
    public static void onXpGain(XpGainPacket msg) {
        ResourceLocation id = msg.categoryId;
        int delta = msg.delta;

        // stack into existing toast if present:
        XpToast existing = ClientNotify.getToastComponent()
                .getToast(XpToast.class, id);

        if (existing != null) {
            existing.addGained(delta);
        } else {
            ClientNotify.getToastComponent()
                    .addToast(new XpToast(id, delta));
        }
    }
}
