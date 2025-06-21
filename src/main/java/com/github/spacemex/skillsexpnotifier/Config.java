package com.github.spacemex.skillsexpnotifier;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<Integer> X_OFFSET; // Starting X Left/Right
    public static final ForgeConfigSpec.ConfigValue<Integer> Y_OFFSET; // Starting Y Up/Down
    public static final ForgeConfigSpec.ConfigValue<String> ANIMATION_DIRECTION; // Left / Right
    public static final ForgeConfigSpec.IntValue MAX_TOASTS;
    public static final ForgeConfigSpec.LongValue ANIMATION_TIME;
    public static final ForgeConfigSpec.BooleanValue AUTOREGISTER_DATA;

    public static final ForgeConfigSpec SPEC;
    static {
        X_OFFSET = BUILDER.comment("The Left / Right Offset Of The Toast").define("Base X",5);
        Y_OFFSET = BUILDER.comment("The Up / Down Offset Of The Toast").define("Base Y",50);
        ANIMATION_DIRECTION = BUILDER.comment("The Direction Of The Toast Animation Options Are Left/Right (Direction It Come in From Left Side Or Right").define("Animation Direction","Left");
        MAX_TOASTS = BUILDER.comment("The Max Amount Of Toasts To Display").defineInRange("Max Toasts",5,1,5);
        ANIMATION_TIME = BUILDER.comment("The Amount Of Time The Toast Animation Will Take").defineInRange("Animation Time",600L,100L,10_000L);
        AUTOREGISTER_DATA = BUILDER.comment("**Extremely Experimental** Automatically Register categories and set the icon ** Disables IconMappings.json **").define("Auto Register Data",false);
        SPEC = BUILDER.build();
    }

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT,SPEC, "SkillExpNotifier/config.toml");
    }

}
