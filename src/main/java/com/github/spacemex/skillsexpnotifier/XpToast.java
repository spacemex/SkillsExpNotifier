package com.github.spacemex.skillsexpnotifier;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import net.puffish.skillsmod.api.Category;

public class XpToast implements Toast {
    private static final ResourceLocation BG = Toast.TEXTURE;
    private final Category category;
    private int gained;
    private long lastUpdateTime;


    public XpToast(Category category, int gained) {
        this.category = category;
        this.gained = gained;
        this.lastUpdateTime = Util.getMillis();
    }

    public void addGained(int delta){
        this.gained += delta;
        this.lastUpdateTime = Util.getMillis();
    }

    private boolean useToastControl(){
        return Config.TOAST_CONTROL.get() && ToastConfig.isToastControlInstalled();
    }


    @Override
    public @NotNull Visibility render(@NotNull GuiGraphics graphics, @NotNull ToastComponent toastGui, long startTime) {
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getMillis();

        // --- BACKGROUND ---
        if (!Config.DISABLE_BACKGROUND.get()) {
            if (Config.BACKGROUND_TRANSLUCENT.get()) {
                float a = Config.TITLE_ALPHA.get() / 255f;
                RenderSystem.setShaderColor(1f, 1f, 1f, a);
            }
            graphics.blit(BG, 0, 0, 0, 32, width(), height());
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        // --- ICON & LABEL DATA ---
        String path = category.getId().getPath();
        String title = formatCategoryName(path);
        ItemStack iconStack = EntryRegistry.getIconFor(path);
        if (!iconStack.isEmpty()) {
            graphics.renderItem(iconStack, 6, 6);
        }

        // --- FORCE INLINE MODE? ---
        if (Config.FORCE_INLINE.get()) {
            // build the combined string via overrides
            String tOverride = Config.TITLE_OVERIDE.get().replace("%title%", title);
            String xOverride = Config.EXP_OVERIDE.get().replace("%exp%", String.valueOf(gained));
            String combined = tOverride + xOverride;

            // bold if either flag is on
            if (Config.BOLD_TITLE.get() || Config.BOLD_EXP.get()) {
                combined = "§l" + combined;
            }

            // compute colors & alpha
            int rgb       = Config.TITLE_COLOR.get() & 0xFFFFFF;
            int alpha     = Config.TITLE_TRANSLUCENT.get() ? Config.TITLE_ALPHA.get() : 255;
            int argb      = (alpha << 24) | rgb;

            // drop shadow if either requested
            if (Config.TITLE_DROPSHADOW.get() || Config.EXP_DROPSHADOW.get()) {
                int sRgb   = Config.TITLE_DROPSHADOW_COLOR.get() & 0xFFFFFF;
                int sAlpha = Config.TITLE_TRANSLUCENT.get() ? Config.TITLE_ALPHA.get() : 255;
                int sArgb  = (sAlpha << 24) | sRgb;
                // shadow one pixel down/right
                graphics.drawString(mc.font, combined, 31, 13, sArgb, false);
            }
            // draw the combined text one pixel above the shadow
            graphics.drawString(mc.font, combined, 30, 12, argb, false);
        }
        // --- NORMAL (two‐line) MODE ---
        else {
            // TITLE
            String titleText = Config.BOLD_TITLE.get() ? "§l" + title : title;
            int titleRgb     = Config.TITLE_COLOR.get() & 0xFFFFFF;
            int titleAlpha   = Config.TITLE_TRANSLUCENT.get() ? Config.TITLE_ALPHA.get() : 255;
            int titleArgb    = (titleAlpha << 24) | titleRgb;

            if (Config.TITLE_DROPSHADOW.get()) {
                int sdRgb   = Config.TITLE_DROPSHADOW_COLOR.get() & 0xFFFFFF;
                int sdAlpha = Config.TITLE_TRANSLUCENT.get() ? Config.TITLE_ALPHA.get() : 255;
                int sdArgb  = (sdAlpha << 24) | sdRgb;
                graphics.drawString(mc.font, titleText, 31, 9, sdArgb, false);
            }
            graphics.drawString(mc.font, titleText, 30, 8, titleArgb, false);

            // XP LINE
            String xpText = (Config.BOLD_EXP.get() ? "§l" : "")
                    + Config.EXP_OVERIDE.get().replace("%exp%", String.valueOf(gained));
            int xpRgb     = Config.EXP_COLOR.get() & 0xFFFFFF;
            int xpAlpha   = Config.EXP_TRANSLUCENT.get() ? Config.EXP_ALPHA.get() : 255;
            int xpArgb    = (xpAlpha << 24) | xpRgb;

            if (Config.EXP_DROPSHADOW.get()) {
                int esRgb   = Config.EXP_DROPSHADOW_COLOR.get() & 0xFFFFFF;
                int esAlpha = Config.EXP_TRANSLUCENT.get() ? Config.EXP_ALPHA.get() : 255;
                int esArgb  = (esAlpha << 24) | esRgb;
                graphics.drawString(mc.font, xpText, 31, 19, esArgb, false);
            }
            graphics.drawString(mc.font, xpText, 30, 18, xpArgb, false);
        }

        // --- VISIBILITY DURATION ---
        return (now - lastUpdateTime) < Config.STACK_XP_TIMER.get()
                ? Visibility.SHOW
                : Visibility.HIDE;
    }



    @Override
    public @NotNull Object getToken() {
        return category.getId();
    }

    public static String formatCategoryName(String rawPath) {
        return java.util.Arrays.stream(rawPath.split("_"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
