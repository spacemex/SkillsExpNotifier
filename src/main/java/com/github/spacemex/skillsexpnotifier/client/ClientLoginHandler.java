package com.github.spacemex.skillsexpnotifier.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.github.spacemex.skillsexpnotifier.Skillsexpnotifier.MODID;

@Mod.EventBusSubscriber(modid = MODID,value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientLoginHandler {
    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event){
        SearchForSkills.scan();
    }
}
