package com.github.spacemex;

import com.github.spacemex.yml.YamlConfigUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

public class XpToast implements Toast {
    private YamlConfigUtil config(){
        ConfigHelper helper = new ConfigHelper();
        return helper.getConfigUtil();
    }

    private static final Identifier BG = Toast.TEXTURE;
    private final Identifier categoryId;
    private int gained;
    private long lastUpdateTime;

    public XpToast(Identifier categoryId, int gained) {
        this.categoryId = categoryId;
        this.gained = gained;
        this.lastUpdateTime = System.currentTimeMillis();
    }


    public void addGained(int delta) {
        this.gained += delta;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public int getGained() {
        return gained;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public int slotCount() {
        return 1;
    }

    @Override
    public Visibility draw(DrawContext context, net.minecraft.client.toast.ToastManager manager, long startTime) {
        MinecraftClient mc = MinecraftClient.getInstance();
        long now = System.currentTimeMillis();

        int bgH = config().getInt("Toast-Rendering.Height",16);
        int bgW = config().getInt("Toast-Rendering.Width",160);

        if (!config().getBoolean("Toast-Rendering.Disable-Background",true)) {
            if (config().getBoolean("Toast-Rendering.Background-Translucent",false)) {
                float a = config().getFloat("Title-Settings.Alpha",127) / 255f;
                RenderSystem.setShaderColor(1f, 1f, 1f, a);
            }
            context.drawTexture(BG, 0, 0, 0, 0,bgW, bgH);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        // Render icon
        String path = categoryId.getPath();
        String title = formatCategoryName(path);
        ItemStack iconStack = EntryRegistry.getIconFor(path);
        if (!iconStack.isEmpty()) {
            float scale = config().getFloat("Icon-Settings.Size",12) / 16f;
            context.getMatrices().push();
            context.getMatrices().scale(scale, scale, 1f);
            int x = (int) (config().getInt("Icon-Settings.X-Offset",14) / scale);
            int y = (int) (config().getInt("Icon-Settings.Y-Offset",2) / scale);
            context.drawItem(iconStack, x, y);
            context.getMatrices().pop();
        }

        BiConsumer<String, Integer> drawTitle = (text, y) -> {
            context.getMatrices().push();
            float scale = config().getFloat("Title-Settings.Size",6) / 9f;
            context.getMatrices().scale(scale, scale, 1f);
            int x = (int) (30f / scale);
            int yy = (int) (y / scale);
            if (config().getBoolean("Title-Settings.Shadow",false)) {
                int shadowColor = (config().getBoolean("Title-Settings.Translucent",false) ? config().getInt("Title-Settings.Alpha",127) : 255) << 24
                        | (config().getInt("Title-Settings.Shadow-Color",0) & 0xFFFFFF);
                context.drawText(mc.textRenderer, text, x + 1, yy + 1, shadowColor, false);
            }
            int mainColor = (config().getBoolean("Title-Settings.Translucent",false) ? config().getInt("Title-Settings.Alpha",127) : 255) << 24
                    | (config().getInt("Title-Settings.Color",16755200) & 0xFFFFFF);
            context.drawText(mc.textRenderer, text, x, yy, mainColor, false);
            context.getMatrices().pop();
        };

        BiConsumer<String, Integer> drawExp = (text, y) -> {
            context.getMatrices().push();
            float scale = config().getFloat("Experience-Settings.Size",6) / 9f;
            context.getMatrices().scale(scale, scale, 1f);
            int x = (int) (30f / scale);
            int yy = (int) (y / scale);
            if (config().getBoolean("Experience-Settings.Shadow",false)) {
                int shadowColor = (config().getBoolean("Experience-Settings.Translucent",false) ? config().getInt("Experience-Settings.Alpha",127) : 255) << 24
                        | (config().getInt("Experience-Settings.Shadow-Color",0) & 0xFFFFFF);
                context.drawText(mc.textRenderer, text, x + 1, yy + 1, shadowColor, false);
            }
            int mainColor = (config().getBoolean("Experience-Settings.Translucent",false) ? config().getInt("Experience-Settings.Alpha",127) : 255) << 24
                    | (config().getInt("Experience-Settings.Color",16755200) & 0xFFFFFF);
            context.drawText(mc.textRenderer, text, x, yy, mainColor, false);
            context.getMatrices().pop();
        };

        if (config().getBoolean("Toast-Animation.Inline",true)) {
            String tOv = config().getString("Title-Settings.Title","%title%").replace("%title%", title);
            String xOv = config().getString("Experience-Settings.Exp"," +%exp% xp").replace("%exp%", String.valueOf(gained));
            String combined = tOv + xOv;
            if (config().getBoolean("Title-Settings.Bold",false) || config().getBoolean("Experience-Settings.Bold",false)) combined = "§l" + combined;
            int midY = bgH / 2 - (config().getInt("Title-Settings.Size",6) / 2);
            drawTitle.accept(combined, midY);
        } else {
            String tt = (config().getBoolean("Title-Settings.Bold",false) ? "§l" : "") + title;
            drawTitle.accept(tt, 8);

            String xp = (config().getBoolean("Experience-Settings.Bold",false) ? "§l" : "")
                    + config().getString("Experience-Settings.Exp"," +%exp% xp").replace("%exp%", String.valueOf(gained));
            drawExp.accept(xp, 18);
        }

        return (now - lastUpdateTime) < config().getLong("Toast-Animation.Stack-XP-Timer",5000)
                ? Visibility.SHOW
                : Visibility.HIDE;
    }
    

    @Override
    public Object getType() {
        return categoryId;
    }


    public static String formatCategoryName(String rawPath) {
        return java.util.Arrays.stream(rawPath.split("_"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
