package com.github.spacemex.skillsexpnotifier.client;

import com.github.spacemex.skillsexpnotifier.Config;
import com.github.spacemex.skillsexpnotifier.Skillsexpnotifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
@OnlyIn( Dist.CLIENT)
public class ToastConfig {
    private static ModConfig TOAST_CONTROL;

    public static boolean isToastControlInstalled(){
        return TOAST_CONTROL != null;
    }

    public static int forceTime(){
        return TOAST_CONTROL.getSpec().getInt("forceTime");
    }

    public static int toastCount(){
        return TOAST_CONTROL.getSpec().getInt("toastCount");
    }






    @Mod.EventBusSubscriber(modid = Skillsexpnotifier.MODID, bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
    static class Configs{
        @SubscribeEvent
        public static void onReload(ModConfigEvent.Reloading configEvent){
            if (configEvent.getConfig().getSpec() == Config.SPEC) {
                Skillsexpnotifier.LOGGER.info("Reloading SkillExpNotifier Config");
            }

        }
        @SubscribeEvent
        public static void onModConfigEvent(ModConfigEvent.Loading e){
            ModConfig cfg = e.getConfig();
            if (!cfg.getModId().equals("toastcontrol")) return;
            Skillsexpnotifier.LOGGER.info("Found ToastControl Config, Enabling Toast Control Overrides...");
            TOAST_CONTROL = cfg;
        }
    }
}
