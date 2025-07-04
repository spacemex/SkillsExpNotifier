package com.github.spacemex.skillsexpnotifier;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final String CONFIG_PATH = "SkillExpNotifier/config.toml";
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
    public static final ForgeConfigSpec.ConfigValue<String> ANCHOR_POSITION;
    public static final ForgeConfigSpec.IntValue EXP_SIZE;
    public static final ForgeConfigSpec.IntValue TITLE_SIZE;
    public static final ForgeConfigSpec.IntValue ICON_SIZE;
    public static final ForgeConfigSpec.IntValue BACKGROUD_SIZE_H;
    public static final ForgeConfigSpec.IntValue BACKGROUD_SIZE_W;
    public static final ForgeConfigSpec.IntValue ICON_X;
    public static final ForgeConfigSpec.IntValue ICON_Y;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FILE_WATCHER;
    public static final ForgeConfigSpec.BooleanValue DISABLE_TOAST_SOUNDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOUND_MAPPINGS_IN;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOUND_MAPPINGS_OUT;
    public static final ForgeConfigSpec.BooleanValue DISABLE_TOASTS;


    public static final ForgeConfigSpec SPEC;
    static {
        BUILDER.push("Settings");
        DISABLE_TOASTS = BUILDER.comment("Completely Disable The Client Side Functionality")
                .define("Disable Toasts",false);
        ENABLE_FILE_WATCHER = BUILDER.comment("Enable The Built-In File Watcher, Checks If This File Is Modified And Reloads The Config(Bypass Forge Reloading)")
                .define("Enable File Watcher",true);
        TOAST_CONTROL = BUILDER.comment("Enable Toast Control")
                .define("Toast Control",false);
        AUTOREGISTER_DATA = BUILDER.comment("**Extremely Experimental** Automatically Register categories and set the icon ** Overrides IconMappings.json **")
                .define("Auto Register Data",false);
        MAX_TOASTS = BUILDER.comment("The Max Amount Of Toasts To Display (Ignored If Toast Control Is Enabled)")
                .defineInRange("Max Toasts",5,1,5);
        ANIMATION_DIRECTION = BUILDER.comment("The Direction Of The Toast Animation Options Are Left/Right (Direction It Come in From Left, Right, Top, Down")
                .define("Animation Direction","Top");
        X_OFFSET = BUILDER.comment("The Left / Right Offset Of The Toast")
                .define("Base X",0);
        Y_OFFSET = BUILDER.comment("The Up / Down Offset Of The Toast")
                .define("Base Y",0);
        ICON_X = BUILDER
                .comment("Horizontal pixel offset for the icon")
                .defineInRange("Icon X Offset", 6, 0, 100);
        ICON_Y = BUILDER
                .comment("Vertical pixel offset for the icon")
                .defineInRange("Icon Y Offset", 6, 0, 100);
        STACK_XP_TIMER = BUILDER.comment("How long (in ms) a stacked/updated XP toast stays visible after the last gain")
                .defineInRange("Stack XP Timer", 1500L, 100L, 10_000L);
        ANIMATION_TIME = BUILDER.comment("How long (in ms) the toast animation takes to complete(Ignored if Toast Control Is Enabled")
                .defineInRange("Animation Time", 800L, 100L, 10_000L);
        NO_SLIDE = BUILDER.comment("Should the toasts slide out of the way when they are added?")
                .define("No Slide",false);
        TITLE_TRANSLUCENT = BUILDER.comment("Should the title be translucent")
                .define("Title Translucent",false);
        EXP_TRANSLUCENT = BUILDER.comment("Should the exp amount be translucent")
                .define("Exp Translucent",false);
        BACKGROUND_TRANSLUCENT = BUILDER.comment("Should the background be translucent")
                .define("Background Translucent",false);
        DISABLE_BACKGROUND = BUILDER.comment("Should the background be disabled")
                .define("Disable Background",false);
        BUILDER.pop();

        BUILDER.push("Text Settings");
        ANCHOR_POSITION = BUILDER
                .comment("""
                        Where to anchor toasts on screen: \
                        "top-left", "top-center", "top-right",
                        "middle-left", "middle-center", "middle-right",
                        "bottom-left", "bottom-center", "bottom-right\"""")
                .define("Anchor Position", "bottom-left");
        TITLE_OVERIDE = BUILDER.comment("The Title Part To Display")
                .define("Title", "%title%");
        EXP_OVERIDE = BUILDER.comment("The Exp Part Of The Message")
                .define("Exp", " +%exp% xp");
        FORCE_INLINE = BUILDER.comment("Should the text be forced to be inline")
                .define("Force Inline",false);
        TITLE_ALPHA = BUILDER.comment("The Alpha Of The Title")
                .defineInRange("Title Alpha",127,0,255);
        EXP_ALPHA = BUILDER.comment("The Alpha Of The Exp Amount")
                .defineInRange("Exp Alpha",127,0,255);

        TITLE_SIZE = BUILDER.comment("The Size Of The Title")
                .defineInRange("Title Size",12,1,32);
        EXP_SIZE = BUILDER.comment("The Size Of The Exp Amount")
                .defineInRange("Exp Size",12,1,32);
        ICON_SIZE = BUILDER.comment("The Size Of The Icon")
                .defineInRange("Icon Size",16,1,32);
        BACKGROUD_SIZE_H = BUILDER.comment("The Height Of The Background")
                .defineInRange("Background Height",32,1,1000);
        BACKGROUD_SIZE_W = BUILDER.comment("The Width Of The Background")
                .defineInRange("Background Width",160,1,1000);
        BUILDER.push("Text Style");
        TITLE_COLOR = BUILDER.comment("The Color Of The Title")
                .define("Title Color",0xFFFFFF);
        EXP_COLOR = BUILDER.comment("The Color Of The Exp Amount")
                .define("Exp Color",0xFFFFFF);
        BOLD_TITLE = BUILDER.comment("Should The Title Be Bold")
                .define("Bold Title",false);
        BOLD_EXP = BUILDER.comment("Should The Exp Amount Be Bold")
                .define("Bold Exp",false);
        TITLE_DROPSHADOW = BUILDER.comment("Should The Title Have A Drop Shadow")
                .define("Title Drop Shadow",false);
        EXP_DROPSHADOW = BUILDER.comment("Should The Exp Amount Have A Drop Shadow")
                .define("Exp Drop Shadow",false);
        EXP_DROPSHADOW_COLOR = BUILDER.comment("The Color Of The Exp Amount Drop Shadow")
                .define("Exp Drop Shadow Color",0x000000);
        TITLE_DROPSHADOW_COLOR = BUILDER.comment("The Color Of The Title Drop Shadow")
                .define("Title Drop Shadow Color",0x000000);

        BUILDER.pop();
        BUILDER.pop();

        BUILDER.push("Sounds");
        DISABLE_TOAST_SOUNDS = BUILDER.comment("Disable All Sounds Made By Toast")
                        .define("Disable Toast Sounds",false);
        List<String> defaultsIn = List.of(
                "minecraft:overworld=minecraft:ui.toast.in",
                "minecraft:the_nether=minecraft:ui.toast.in",
                "minecraft:the_end=minecraft:ui.toast.in"
        );
        SOUND_MAPPINGS_IN = BUILDER
                .comment("A list of \"dimension=sound_event\" entries for the toast-in sound")
                .defineList("Sound Mapping In", defaultsIn,
                        o -> (o instanceof String) && ((String)o).contains("=")
                );
        List<String> defaultsOut = List.of(
                "minecraft:overworld=minecraft:ui.toast.out",
                "minecraft:the_nether=minecraft:ui.toast.out",
                "minecraft:the_end=minecraft:ui.toast.out"
        );
        SOUND_MAPPINGS_OUT = BUILDER
                .comment("A list of \"dimension=sound_event\" entries for the toast-out sound")
                .defineList("Sound Mapping Out", defaultsOut,
                        o -> (o instanceof String) && ((String)o).contains("=")
                );

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT,SPEC, CONFIG_PATH);
    }

    private static Map<String,String> parseMappings(List<String> raw) {
        var map = new HashMap<String,String>();
        for (String entry : raw) {
            var split = entry.split("=", 2);
            if (split.length == 2) {
                map.put(split[0].trim(), split[1].trim());
            }
        }
        return map;
    }

    public static String getSoundInForDimension(String dimId) {
        Map<String,String> m = parseMappings((List<String>) Config.SOUND_MAPPINGS_IN.get());
        return m.getOrDefault(dimId, "minecraft:ui.toast.in");
    }

    public static String getSoundOutForDimension(String dimId) {
        Map<String,String> m = parseMappings((List<String>) Config.SOUND_MAPPINGS_OUT.get());
        return m.getOrDefault(dimId, "minecraft:ui.toast.out");
    }

}
