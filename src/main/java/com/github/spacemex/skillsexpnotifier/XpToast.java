package com.github.spacemex.skillsexpnotifier;

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
    private final int gained;

    public XpToast(Category category, int gained) {
        this.category = category;
        this.gained = gained;
    }

    @Override
    public @NotNull Visibility render(GuiGraphics graphics, @NotNull ToastComponent toastGui, long startTime) {
        Minecraft mc = Minecraft.getInstance();

        // draw background
        graphics.blit(BG, 0, 0, 0, 32, width(), height());

        // category path (e.g. "mining_skills")
        String path = category.getId().getPath();

        // 1) format the title nicely
        String title = formatCategoryName(path);
        // 2) lookup icon via your EntryRegistry
        ItemStack iconStack = EntryRegistry.getIconFor(path);

        // draw icon if present
        if (!iconStack.isEmpty()) {
            graphics.renderItem(iconStack, 6, 6);
        }

        // prepare title text with bold if configured
        String titleText = Config.BOLD_TITLE.get() ? "§l" + title : title;
        // draw title drop shadow if configured
        if (Config.TITLE_DROPSHADOW.get()) {
            graphics.drawString(mc.font, titleText, 31, 9, Config.TITLE_DROPSHADOW_COLOR.get(), false);
        }
        // draw title text
        graphics.drawString(mc.font, titleText, 30, 8, Config.TITLE_COLOR.get(), false);

        // prepare XP text with bold if configured
        String xpText = (Config.BOLD_EXP.get() ? "§l" : "") + "+" + gained + " XP";
        // draw XP drop shadow if configured
        if (Config.EXP_DROPSHADOW.get()) {
            graphics.drawString(mc.font, xpText, 31, 19, Config.EXP_DROPSHADOW_COLOR.get(), false);
        }
        // draw XP text
        graphics.drawString(mc.font, xpText, 30, 18, Config.EXP_COLOR.get(), false);

        // duration
        return (startTime < 1_000L) ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public @NotNull Object getToken() {
        return NO_TOKEN;
    }

    public static String formatCategoryName(String rawPath) {
        return java.util.Arrays.stream(rawPath.split("_"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
