package com.github.spacemex.skillexpnotifier.config;

import com.github.spacemex.skillexpnotifier.Skillexpnotifier;
import com.github.spacemex.yml.YamlConfigTemplateWriter;

import java.io.File;
import java.util.Arrays;

public class Config {
    public static void generateConfig(){
        File yamlFile = new File(Skillexpnotifier.CONFIG_FILE.toUri());
        yamlFile.getParentFile().mkdirs();
        YamlConfigTemplateWriter writer = new YamlConfigTemplateWriter(Skillexpnotifier.CONFIG_FILE.toFile());

        writer
                .header("Client-side configuration for SkillExpNotifier")
                .add("Client-Settings.Disable",false,"Disables All Rendering Of The Toasts (Client-Side only)")
                .add("Client-Settings.File-Watcher",true,"Enables The File Watcher (Looks Of Config Updates)")
                .add("Client-Settings.Auto-Register-Data",true, "Automatically register categories and sets the icon (Overrides existing entries)")

                .add("Settings.Max-Toasts",1,"Maximum number of toasts to show at once")
                .add("Settings.Animation-Direction","down","The Direction Of The Toast Animation (top, down, left, right)")

                .add("Toast-Rendering.Anchor-Point","bottom-left", "Where the toast should be anchored to the screen.Options: top-left,top-center,top-right, middle-left,middle-center,middle-right, bottom-left,bottom-center,bottom-right")
                .add("Toast-Rendering.Base-X",0,"The Left / Right offset of the toast")
                .add("Toast-Rendering.Base-Y", 0,"The Top / Bottom offset of the toast")
                .add("Toast-Rendering.Height",16,"The Height Of The Toasts Background")
                .add("Toast-Rendering.Width",160,"The Width Of The Toasts Background")
                .add("Toast-Rendering.Background-Translucent",false,"Force The Toasts Background to be Translucent")
                .add("Toast-Rendering.Disable-Background",true,"Disable The Rendering Of The Toasts Background")
                .add("Toast-Rendering.Background-alpha",127,"Disable The Rendering Of The Toasts Background")

                .add("Toast-Animation.Stack-XP-Timer", 5000,"How long (in ms) to a stacked / updated toast stays visible after the last update")
                .add("Toast-Animation.Animation-Time",1000,"How long (in ms) the toast animation takes to complete")
                .add("Toast-Animation.No-Slide",false,"Should the Toast not slide in/out of the screen?")
                .add("Toast-Animation.Inline",true,"Force The Text Elements to be inline")

                .add("Title-Settings.Title","%title%","The Text Displayed In The Title Of The Toast")
                .add("Title-Settings.Size",6,"The Size Of The Title (in points)")
                .add("Title-Settings.Color",16755200,"The Color Of The Title (in ARGB format)")
                .add("Title-Settings.Shadow",false,"Should The Title Have A Shadow?")
                .add("Title-Settings.Shadow-Color", 0,"The Color Of The Title Shadow (in ARGB format)")
                .add("Title-Settings.Translucent",false,"Should The Title Be Translucent?")
                .add("Title-Settings.Alpha",127,"The Alpha Of The Title (0-255)")
                .add("Title-Settings.Bold",false,"Should The Title Be Bold?")

                .add("Experience-Settings.Exp"," +%exp% xp","The Text Displayed In The Experience Of The Toast")
                .add("Experience-Settings.Size",6,"The Size Of The Experience (in points)")
                .add("Experience-Settings.Color",16755200,"The Color Of The Experience (in ARGB format)")
                .add("Experience-Settings.Shadow",false,"Should The Experience Have A Shadow?")
                .add("Experience-Settings.Shadow-Color", 0,"The Color Of The Experience Shadow (in ARGB format)")
                .add("Experience-Settings.Translucent",false,"Should The Experience Be Translucent?")
                .add("Experience-Settings.Alpha",127,"The Alpha Of The Exp (0-255)")
                .add("Experience-Settings.Bold",false,"Should The Experience Be Bold?")

                .add("Icon-Settings.Enabled",true,"Should The Icon Be Enabled?")
                .add("Icon-Settings.X-Offset",14,"The X Offset Of The Icon")
                .add("Icon-Settings.Y-Offset",2,"The Y Offset of The Icon")
                .add("Icon-Settings.Size",12,"The Size Of The Icon (in pixels)")

                .add("Sound-Settings.Enabled",true,"Should The Sound Be Enabled?")
                .add("Sound-Settings.In-Sound", Arrays.asList("minecraft:overworld=minecraft:ui.toast.in", "minecraft:the_nether=minecraft:ui.toast.in", "minecraft:the_end=minecraft:ui.toast.in"),
                        "Changes The Sound Played When The Toast Appears Per Dimension (Format: <dimension>=<sound>)")
                .add("Sound-Settings.Out-Sound",Arrays.asList("minecraft:overworld=minecraft:ui.toast.out", "minecraft:the_nether=minecraft:ui.toast.out", "minecraft:the_end=minecraft:ui.toast.out"),
                        "Changes The Sound Played When The Toast Disappears Per Dimension (Format: <dimension>=<sound>)")
                .write();
    }
}
