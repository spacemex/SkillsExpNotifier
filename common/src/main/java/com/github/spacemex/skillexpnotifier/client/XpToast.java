package com.github.spacemex.skillexpnotifier.client;

import com.github.spacemex.skillexpnotifier.config.ConfigReader;
import com.github.spacemex.yml.YamlConfigUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class XpToast implements Toast {
    private static final ResourceLocation BG = TEXTURE;
    private final ResourceLocation categoryId;
    private int gained;
    private long lastUpdateTime;

    public XpToast(ResourceLocation categoryId, int gained) {
        this.categoryId = categoryId;
        this.gained = gained;
        this.lastUpdateTime = Util.getMillis();

    }

    public void addGained(int delta){
        this.gained += delta;
        this.lastUpdateTime = Util.getMillis();
    }
    private YamlConfigUtil config(){
        ConfigReader helper = new ConfigReader();
        return helper.getConfig();
    }
    public int getGained() {
        return gained;
    }


    @Override
    public @NotNull Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l) {
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getMillis();

        // Background
        int bgH = config().getInt("Toast-Rendering.Height",16);
        int bgW = config().getInt("Toast-Rendering.Width",160);

        if (!config().getBoolean("Toast-Rendering.Disable-Background",true)){
            if (config().getBoolean("Toast-Rendering.Background-Translucent",false)){
                float alpha = config().getFloat("Toast-Rendering.Background-Alpha",127) / 255f;
                RenderSystem.setShaderColor(1f,1f,1f,alpha);
            }
            guiGraphics.blit(BG,0,0,0,0,bgW,bgH);
            RenderSystem.setShaderColor(1f,1f,1f,1f);
        }

        // Icon
        String path = categoryId.getPath();
        String title = formatCategoryName(path);
        ItemStack iconStack = EntryRegistry.getIconFor(path);

        if(config().getBoolean("Icon-Settings.Enabled",true)){
            if (!iconStack.isEmpty()){
                float iconScale = config().getFloat("Icon-Settings.Size",12) / 16f;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(iconScale,iconScale,1f);

                int x0 = config().getInt("Icon-Settings.X-Offset",14);
                int y0 = config().getInt("Icon-Settings.Y-Offset",2);

                guiGraphics.renderItem(iconStack,x0,y0);
                guiGraphics.pose().popPose();
            }
        }

        // Title
        BiConsumer<String, Integer> drawTitle = (text, y) -> {
            guiGraphics.pose().pushPose();
            float scale = config().getFloat("Title-Settings.Size",6) / 9f;
            guiGraphics.pose().scale(scale, scale, 1f);
            int x = (int) (30f / scale);
            int yy = (int) (y / scale);

            // Drop Shadow
            if (config().getBoolean("Title-Settings.Shadow",false)){
                int sdRgb = config().getInt("Title-Settings.Shadow-Color",0) & 0xFFFFFF;
                int sdAlpha = config().getBoolean("Title-Settings.Translucent",false) ?
                        config().getInt("Title-Settings.Alpha",127) : 255;
                int sdArgb = (sdAlpha << 24) | sdRgb;
                guiGraphics.drawString(mc.font, text, x + 1, yy + 1, sdArgb, false);
            }

            int rgb = config().getInt("Title-Settings.Color",16755200) & 0xFFFFFF;
            int alpha = config().getBoolean("Title-Settings.Translucent",false) ?
                    config().getInt("Title-Settings.Alpha",127) : 255;
            int argb = (alpha << 24) | rgb;

            guiGraphics.drawString(mc.font, text, x, yy, argb, false);
            guiGraphics.pose().popPose();
        };

        // Exp
        BiConsumer<String, Integer> drawExp = (text, y) -> {
            guiGraphics.pose().pushPose();
            float scale = config().getFloat("Experience-Settings.Size",6) / 9f;
            guiGraphics.pose().scale(scale, scale, 1f);
            int x = (int) (30f / scale);
            int yy = (int) (y / scale);

            // Drop Shadow
            if (config().getBoolean("Experience-Settings.Shadow",false)) {
                int sdRgb = config().getInt("Experience-Settings.Shadow-Color",0) & 0xFFFFFF;
                int sdAlpha = config().getBoolean("Experience-Settings.Translucent",false) ?
                        config().getInt("Experience-Settings.Alpha",127) : 255;
                int sdArgb = (sdAlpha << 24) | sdRgb;
                guiGraphics.drawString(mc.font, text, x + 1, yy + 1, sdArgb, false);
            }

            int rgb2 = config().getInt("Experience-Settings.Color",16755200) & 0xFFFFFF;
            int alpha2 = config().getBoolean("Experience-Settings.Translucent",false) ?
                    config().getInt("Experience-Settings.Alpha",127) : 255;
            int argb2 = (alpha2 << 24) | rgb2;

            guiGraphics.drawString(mc.font, text, x, yy, argb2, false);
            guiGraphics.pose().popPose();
        };

        //Inline vs. Two Line
        if (config().getBoolean("Toast-Animation.Inline",true)){
            String t0v = config().getString("Title-Settings.Title","%title%")
                    .replace("%title%",title);
            String x0v = config().getString("Experience-Settings.Exp"," +%exp% xp")
                    .replace("%exp%",String.valueOf(gained));
            String combined = t0v + " " + x0v;
            if (config().getBoolean("Title-Settings.Bold",false) ||
            config().getBoolean("Experience-Settings.Bold",false)) combined = "§l" + combined;

            int midY = bgH / 2 - (config().getInt("Title-Settings.Size",6) / 2);

            drawTitle.accept(combined, midY);
        }else {
            String tt = (config().getBoolean("Title-Settings.Bold",false) ? "§l" : "") + title;
            drawTitle.accept(tt, 8);

            String xp = (config().getBoolean("Experience-Settings.Bold",false) ? "§l" : "")
                    + config().getString("Experience-Settings.Exp"," +%exp% xp")
                    .replace("%exp%", String.valueOf(gained));
            drawExp.accept(xp, 18);
        }
        return (now - lastUpdateTime) < config().getLong("Toast-Animation.Stack-XP-Timer",5000) ?
                Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public @NotNull Object getToken() {
        return categoryId;
    }

    public static String formatCategoryName(String path){
        return java.util.Arrays.stream(path.split("_"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}
