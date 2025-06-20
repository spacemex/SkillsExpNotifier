package com.github.spacemex.skillsexpnotifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;
import net.puffish.skillsmod.api.Category;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class XpToast implements Toast {
    private static final ResourceLocation BG = Toast.TEXTURE;
    private final Category category;
    private final int gained;

    public XpToast(Category category, int gained) {
        this.category = category;
        this.gained = gained;
    }

    public static String formatCategoryName(String rawPath) {
        return Arrays.stream(rawPath.split("_"))
                .map(word -> word.isEmpty()
                        ? word
                        : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
    @Override
    public @NotNull Visibility render(GuiGraphics graphics, @NotNull ToastComponent toastGui, long startTime) {
        Minecraft mc = Minecraft.getInstance();

        graphics.blit(BG, 0, 0, 0, 32, width(), height());

        ResourceLocation id = category.getId();
        String Title = formatCategoryName(id.getPath());
        String Value  = " + " + gained + " XP";

        graphics.drawString(mc.font, Title, 30, 8, 0xFFFFFF, false);
        graphics.drawString(mc.font, Value, 30, 18, 0xFFFFFF, false);

        var icon = EntryRegistry.getIconFor(id.getPath());
        if (!icon.isEmpty()) {
            graphics.renderItem(icon,6,6);
        }
        return (startTime < 1_000L) ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override public @NotNull Object getToken() { return NO_TOKEN; }

}
