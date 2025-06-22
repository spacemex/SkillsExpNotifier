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

import java.util.function.BiConsumer;

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

    public void addGained(int delta) {
        this.gained += delta;
        this.lastUpdateTime = Util.getMillis();
    }

    private boolean useToastControl() {
        return Config.TOAST_CONTROL.get() && ToastConfig.isToastControlInstalled();
    }


    @Override
    public @NotNull Visibility render(@NotNull GuiGraphics graphics,
                                      @NotNull ToastComponent toastGui,
                                      long startTime) {
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getMillis();

        // ─── BACKGROUND ─────────────────────────────────────────────────────────
        int bgH = Config.BACKGROUD_SIZE_H.get();
        int bgW = Config.BACKGROUD_SIZE_W.get();
        if (!Config.DISABLE_BACKGROUND.get()) {
            if (Config.BACKGROUND_TRANSLUCENT.get()) {
                float a = Config.TITLE_ALPHA.get() / 255f;
                RenderSystem.setShaderColor(1f, 1f, 1f, a);
            }
            // draw a slice of the toast texture at (0,0), height=bgH
            graphics.blit(BG, 0, 0, 0, 0, bgW, bgH);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        // ─── ICON ────────────────────────────────────────────────────────────────
        String path = category.getId().getPath();
        String title = formatCategoryName(path);
        ItemStack iconStack = EntryRegistry.getIconFor(path);
        if (!iconStack.isEmpty()) {
            float iconScale = Config.ICON_SIZE.get() / 16f;
            graphics.pose().pushPose();
            graphics.pose().scale(iconScale, iconScale, 1f);

            int x0 = (int)(Config.ICON_X.get() / iconScale);
            int y0 = (int)(Config.ICON_Y.get() / iconScale);

            graphics.renderItem(iconStack, x0, y0);
            graphics.pose().popPose();
        }

        // ─── TEXT DRAWING HELPERS ────────────────────────────────────────────────
        // Title drawing at Y=8 normally, scaled by TITLE_SIZE/9f
        BiConsumer<String, Integer> drawTitle = (text, y) -> {
            graphics.pose().pushPose();
            float scale = Config.TITLE_SIZE.get() / 9f;
            graphics.pose().scale(scale, scale, 1f);
            int x = (int) (30f / scale);
            int yy = (int) (y / scale);

            if (Config.TITLE_DROPSHADOW.get()) {
                int sdRgb = Config.TITLE_DROPSHADOW_COLOR.get() & 0xFFFFFF;
                int sdAlpha = Config.TITLE_TRANSLUCENT.get() ? Config.TITLE_ALPHA.get() : 255;
                int sdArgb = (sdAlpha << 24) | sdRgb;
                graphics.drawString(mc.font, text, x + 1, yy + 1, sdArgb, false);
            }
            int rgb = Config.TITLE_COLOR.get() & 0xFFFFFF;
            int alpha = Config.TITLE_TRANSLUCENT.get() ? Config.TITLE_ALPHA.get() : 255;
            int argb = (alpha << 24) | rgb;
            graphics.drawString(mc.font, text, x, yy, argb, false);
            graphics.pose().popPose();
        };

        // XP drawing at Y=18 normally, scaled by EXP_SIZE/9f
        BiConsumer<String, Integer> drawExp = (text, y) -> {
            graphics.pose().pushPose();
            float scale = Config.EXP_SIZE.get() / 9f;
            graphics.pose().scale(scale, scale, 1f);
            int x = (int) (30f / scale);
            int yy = (int) (y / scale);

            if (Config.EXP_DROPSHADOW.get()) {
                int sdRgb = Config.EXP_DROPSHADOW_COLOR.get() & 0xFFFFFF;
                int sdAlpha = Config.EXP_TRANSLUCENT.get() ? Config.EXP_ALPHA.get() : 255;
                int sdArgb = (sdAlpha << 24) | sdRgb;
                graphics.drawString(mc.font, text, x + 1, yy + 1, sdArgb, false);
            }
            int rgb2 = Config.EXP_COLOR.get() & 0xFFFFFF;
            int alpha2 = Config.EXP_TRANSLUCENT.get() ? Config.EXP_ALPHA.get() : 255;
            int argb2 = (alpha2 << 24) | rgb2;
            graphics.drawString(mc.font, text, x, yy, argb2, false);
            graphics.pose().popPose();
        };

        // ─── INLINE vs TWO-LINE ───────────────────────────────────────────────────
        if (Config.FORCE_INLINE.get()) {
            String tOv = Config.TITLE_OVERIDE.get().replace("%title%", title);
            String xOv = Config.EXP_OVERIDE.get().replace("%exp%", String.valueOf(gained));
            String combined = tOv + xOv;
            if (Config.BOLD_TITLE.get() || Config.BOLD_EXP.get()) combined = "§l" + combined;
            // draw inline, vertically centered in bgH (≈midY)
            int midY = bgH / 2 - (Config.TITLE_SIZE.get() / 2);
            drawTitle.accept(combined, midY);
        } else {
            // two-line: title @ y=8, xp @ y=18
            String tt = (Config.BOLD_TITLE.get() ? "§l" : "") + title;
            drawTitle.accept(tt, 8);

            String xp = (Config.BOLD_EXP.get() ? "§l" : "")
                    + Config.EXP_OVERIDE.get().replace("%exp%", String.valueOf(gained));
            drawExp.accept(xp, 18);
        }

        // ─── DURATION ────────────────────────────────────────────────────────────
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
