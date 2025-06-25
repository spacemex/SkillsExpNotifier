package com.github.spacemex.skillsexpnotifier.client;

import com.github.spacemex.skillsexpnotifier.XpGainPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToastPacketHandler {
    public static void onXpGain(XpGainPacket msg) {
        ResourceLocation id = msg.categoryId;
        int delta = msg.delta;

        // stack into existing toast if present:
        XpToast existing = NotifierClient.getToastComponent()
                .getToast(XpToast.class, id);

        if (existing != null) {
            existing.addGained(delta);
        } else {
            NotifierClient.getToastComponent()
                    .addToast(new XpToast(id, delta));
        }
    }
}
