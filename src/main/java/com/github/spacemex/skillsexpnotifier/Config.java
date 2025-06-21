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
    public static final ForgeConfigSpec.BooleanValue BOLD_TITLE;
    public static final ForgeConfigSpec.BooleanValue BOLD_EXP;
    public static final ForgeConfigSpec.BooleanValue TITLE_DROPSHADOW;
    public static final ForgeConfigSpec.BooleanValue EXP_DROPSHADOW;
    public static final ForgeConfigSpec.ConfigValue<Integer> TITLE_COLOR;
    public static final ForgeConfigSpec.ConfigValue<Integer> EXP_COLOR;
    public static final ForgeConfigSpec.ConfigValue<Integer> EXP_DROPSHADOW_COLOR;
    public static final ForgeConfigSpec.ConfigValue<Integer> TITLE_DROPSHADOW_COLOR;

    public static final ForgeConfigSpec SPEC;
    static {
        BUILDER.push("GUI Settings");
        X_OFFSET = BUILDER.comment("The Left / Right Offset Of The Toast").define("Base X",5);
        Y_OFFSET = BUILDER.comment("The Up / Down Offset Of The Toast").define("Base Y",50);
        MAX_TOASTS = BUILDER.comment("The Max Amount Of Toasts To Display").defineInRange("Max Toasts",5,1,5);
        ANIMATION_DIRECTION = BUILDER.comment("The Direction Of The Toast Animation Options Are Left/Right (Direction It Come in From Left Side Or Right").define("Animation Direction","Left");
        ANIMATION_TIME = BUILDER.comment("The Amount Of Time The Toast Animation Will Take").defineInRange("Animation Time",600L,100L,10_000L);
        BUILDER.pop();
        BUILDER.push("Text Settings");
        TITLE_COLOR = BUILDER.comment("The Color Of The Title").define("Title Color",0xFFFFFF);
        EXP_COLOR = BUILDER.comment("The Color Of The Exp Amount").define("Exp Color",0xFFFFFF);
        BOLD_TITLE = BUILDER.comment("Should The Title Be Bold").define("Bold Title",false);
        BOLD_EXP = BUILDER.comment("Should The Exp Amount Be Bold").define("Bold Exp",false);
        TITLE_DROPSHADOW = BUILDER.comment("Should The Title Have A Drop Shadow").define("Title Drop Shadow",false);
        EXP_DROPSHADOW = BUILDER.comment("Should The Exp Amount Have A Drop Shadow").define("Exp Drop Shadow",false);
        EXP_DROPSHADOW_COLOR = BUILDER.comment("The Color Of The Exp Amount Drop Shadow").define("Exp Drop Shadow Color",0x000000);
        TITLE_DROPSHADOW_COLOR = BUILDER.comment("The Color Of The Title Drop Shadow").define("Title Drop Shadow Color",0x000000);
        BUILDER.pop();
        BUILDER.push("Experimental Settings");
        AUTOREGISTER_DATA = BUILDER.comment("**Extremely Experimental** Automatically Register categories and set the icon ** Overrides IconMappings.json **").define("Auto Register Data",false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT,SPEC, "SkillExpNotifier/config.toml");
    }

}
