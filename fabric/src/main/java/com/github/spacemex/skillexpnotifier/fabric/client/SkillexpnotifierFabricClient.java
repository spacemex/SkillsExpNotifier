package com.github.spacemex.skillexpnotifier.fabric.client;

import com.github.spacemex.skillexpnotifier.client.EntryRegistry;
import com.github.spacemex.skillexpnotifier.client.XpToast;
import com.github.spacemex.skillexpnotifier.discovery.SearchForSkills;
import com.github.spacemex.skillexpnotifier.fabric.SkillexpnotifierFabric;
import com.github.spacemex.skillexpnotifier.fabric.network.client.ClientNotify;
import com.github.spacemex.skillexpnotifier.network.XpGainPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class SkillexpnotifierFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            ClientNotify.getToastComponent().render(drawContext);
        });

        ClientPlayNetworking.registerGlobalReceiver(SkillexpnotifierFabric.XP_GAIN_PACKET_ID, (client, handler, buf, responseSender) -> {
            XpGainPacket packet = XpGainPacket.decode(buf);
            client.execute(()->{
                ClientNotify.getToastComponent().addToast(new XpToast(packet.categoryId, packet.delta));
            });
        });

        EntryRegistry.loadFromFile();
        SearchForSkills.scan();
    }
}
