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
    public static final ForgeConfigSpec.LongValue STACK_XP_TIMER;
    public static final ForgeConfigSpec.BooleanValue TOAST_CONTROL;

    public static final ForgeConfigSpec.BooleanValue TITLE_TRANSLUCENT;
    public static final ForgeConfigSpec.BooleanValue EXP_TRANSLUCENT;
    public static final ForgeConfigSpec.BooleanValue BACKGROUND_TRANSLUCENT;
    public static final ForgeConfigSpec.BooleanValue DISABLE_BACKGROUND;

    public static final ForgeConfigSpec.IntValue TITLE_ALPHA;
    public static final ForgeConfigSpec.IntValue EXP_ALPHA;
    public static final ForgeConfigSpec.BooleanValue NO_SLIDE;
    public static final ForgeConfigSpec.BooleanValue FORCE_INLINE;
    public static final ForgeConfigSpec.ConfigValue<String> TITLE_OVERIDE;
    public static final ForgeConfigSpec.ConfigValue<String> EXP_OVERIDE;


    public static final ForgeConfigSpec SPEC;
    static {
        BUILDER.push("Settings");
        TOAST_CONTROL = BUILDER.comment("Enable Toast Control").define("Toast Control",false);
        AUTOREGISTER_DATA = BUILDER.comment("**Extremely Experimental** Automatically Register categories and set the icon ** Overrides IconMappings.json **").define("Auto Register Data",false);
        MAX_TOASTS = BUILDER.comment("The Max Amount Of Toasts To Display (Ignored If Toast Control Is Enabled)").defineInRange("Max Toasts",5,1,5);
        ANIMATION_DIRECTION = BUILDER.comment("The Direction Of The Toast Animation Options Are Left/Right (Direction It Come in From Left, Right, Top, Down").define("Animation Direction","Left");
        X_OFFSET = BUILDER.comment("The Left / Right Offset Of The Toast").define("Base X",5);
        Y_OFFSET = BUILDER.comment("The Up / Down Offset Of The Toast").define("Base Y",50);
        STACK_XP_TIMER = BUILDER.comment("How long (in ms) a stacked/updated XP toast stays visible after the last gain").defineInRange("Stack XP Timer", 1000L, 100L, 10_000L);
        ANIMATION_TIME = BUILDER.comment("How long (in ms) the toast animation takes to complete(Ignored if Toast Control Is Enabled").defineInRange("Animation Time", 1000L, 100L, 10_000L);
        NO_SLIDE = BUILDER.comment("Should the toasts slide out of the way when they are added?").define("No Slide",false);
        TITLE_TRANSLUCENT = BUILDER.comment("Should the title be translucent").define("Title Translucent",false);
        EXP_TRANSLUCENT = BUILDER.comment("Should the exp amount be translucent").define("Exp Translucent",false);
        BACKGROUND_TRANSLUCENT = BUILDER.comment("Should the background be translucent").define("Background Translucent",false);
        DISABLE_BACKGROUND = BUILDER.comment("Should the background be disabled").define("Disable Background",false);
        BUILDER.pop();

        BUILDER.push("Text Settings");
        TITLE_OVERIDE = BUILDER.comment("The Title Part To Display").define("Title", "%title%");
        EXP_OVERIDE = BUILDER.comment("The Exp Part Of The Message").define("Exp", " +%exp% xp");
        FORCE_INLINE = BUILDER.comment("Should the text be forced to be inline").define("Force Inline",false);
        TITLE_ALPHA = BUILDER.comment("The Alpha Of The Title").defineInRange("Title Alpha",127,0,255);
        EXP_ALPHA = BUILDER.comment("The Alpha Of The Exp Amount").defineInRange("Exp Alpha",127,0,255);

        BUILDER.push("Text Style");
        TITLE_COLOR = BUILDER.comment("The Color Of The Title").define("Title Color",0xFFFFFF);
        EXP_COLOR = BUILDER.comment("The Color Of The Exp Amount").define("Exp Color",0xFFFFFF);
        BOLD_TITLE = BUILDER.comment("Should The Title Be Bold").define("Bold Title",false);
        BOLD_EXP = BUILDER.comment("Should The Exp Amount Be Bold").define("Bold Exp",false);
        TITLE_DROPSHADOW = BUILDER.comment("Should The Title Have A Drop Shadow").define("Title Drop Shadow",false);
        EXP_DROPSHADOW = BUILDER.comment("Should The Exp Amount Have A Drop Shadow").define("Exp Drop Shadow",false);
        EXP_DROPSHADOW_COLOR = BUILDER.comment("The Color Of The Exp Amount Drop Shadow").define("Exp Drop Shadow Color",0x000000);
        TITLE_DROPSHADOW_COLOR = BUILDER.comment("The Color Of The Title Drop Shadow").define("Title Drop Shadow Color",0x000000);
        BUILDER.pop();
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT,SPEC, "SkillExpNotifier/config.toml");
    }

}
