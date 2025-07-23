package com.github.spacemex;

import com.github.spacemex.yml.YamlConfigUtil;
import com.google.common.collect.Queues;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.*;

@Environment(EnvType.CLIENT)
public class CustomToastComponent {
    private final MinecraftClient minecraft;
    private final List<CustomToastInstance<?>> visible = new ArrayList<>();
    private final Deque<Toast> queued = Queues.newArrayDeque();
    private boolean needsReindexing = false;

    public CustomToastComponent(MinecraftClient minecraft) {
        this.minecraft = Objects.requireNonNull(minecraft);
    }

    private YamlConfigUtil config(){
        ConfigHelper helper = new ConfigHelper();
        return helper.getConfigUtil();
    }

    public void addToast(Toast toast) {
        if (config().getBoolean("Client-Settings.Disable",false)) return;

        // Try to merge with visible toasts
        XpToast existingVisible = getToast(XpToast.class, toast.getType());
        if (existingVisible != null && toast instanceof XpToast incoming) {
            existingVisible.addGained(incoming.getGained());
            SkillExpNotifierFabric.LOGGER.info("Merged visible toast {} and {}", existingVisible, toast);
            return;
        }

        // Try to merge with queued toasts
        for (Toast queuedToast : queued) {
            if (queuedToast instanceof XpToast queuedXp
                    && queuedToast.getClass() == toast.getClass()
                    && Objects.equals(queuedToast.getType(), toast.getType())) {
                queuedXp.addGained(((XpToast) toast).getGained());
                return;
            }
        }

        queued.add(toast);
    }

    public void render(DrawContext graphics) {
        if (minecraft.options.hudHidden) return;

        int screenWidth = minecraft.getWindow().getScaledWidth();

        // First, check if we need to reindex the toasts
        if (needsReindexing) {
            reindexToasts();
            needsReindexing = false;
        }

        // Render existing toasts and remove finished ones
        Iterator<CustomToastInstance<?>> it = visible.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            CustomToastInstance<?> instance = it.next();
            if (instance.render(screenWidth, graphics)) {
                it.remove();
                removed = true;
            }
        }

        // If we removed any toasts, we need to reindex the remaining ones
        if (removed) {
            needsReindexing = true;
        }

        // Add new toasts from the queue if there's space
        int maxToasts = getSlotCount();
        while (!queued.isEmpty() && visible.size() < maxToasts) {
            Toast toast = queued.removeFirst();
            int idx = visible.size(); // Use the next available index
            visible.add(new CustomToastInstance<>(toast, idx, 1));
        }
    }

    // Reindex toasts to ensure they appear in sequential order without gaps
    private void reindexToasts() {
        for (int i = 0; i < visible.size(); i++) {
            CustomToastInstance<?> instance = visible.get(i);
            instance.index = i;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Toast> T getToast(Class<? extends T> toastClass, Object token) {
        for (CustomToastInstance<?> inst : visible) {
            Toast t = inst.toast;
            if (toastClass.isAssignableFrom(t.getClass()) && t.getType().equals(token)) {
                return (T) t;
            }
        }
        for (Toast t : queued) {
            if (toastClass.isAssignableFrom(t.getClass()) && t.getType().equals(token)) {
                return (T) t;
            }
        }
        return null;
    }

    private int getSlotCount() {
        int maxToasts = config().getInt("Settings.Max-Toasts",5);
        return Math.max(1, maxToasts);
    }

    private long getAnimationTime() {
        return config().getLong("Toast-Animation.Animation-Time",1000) <= 0 ? 6000L : config().getLong("Toast-Animation.Animation-Time",1000);
    }

    private class CustomToastInstance<T extends Toast> {
        private final T toast;
        private int index;
        private final int slotCount;
        private long animationTime = -1L;
        private long visibleTime = -1L;
        private Toast.Visibility visibility = Toast.Visibility.SHOW;
        private float targetY = -1f; // Target Y position for smooth repositioning
        private float currentY = -1f; // Current Y position for smooth repositioning

        CustomToastInstance(T toast, int index, int slotCount) {
            this.toast = toast;
            this.index = index;
            this.slotCount = slotCount;

            if (toast instanceof XpToast xpToast) {
                xpToast.addGained(0);
            }
        }

        public boolean render(int screenWidth, DrawContext graphics) {
            long now = System.currentTimeMillis();
            if (animationTime < 0) {
                animationTime = now;
                if (config().getBoolean("Sound-Settings.Enabled",true)) {
                    String dimensionId = minecraft.player.getWorld().getRegistryKey().getValue().toString();
                    String soundId = getSoundOutForDimension(dimensionId);

                    try {
                        Identifier soundIdentifier = new Identifier(soundId);
                        SoundEvent soundEvent = SoundEvent.of(soundIdentifier);
                        minecraft.getSoundManager().play(PositionedSoundInstance.master(soundEvent, 1.0F));
                    } catch (Exception e) {
                        // Fallback to default sound if there's an error
                        SkillExpNotifierFabric.LOGGER.error("Failed to play custom out sound: " + soundId, e);
                        minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_OUT, 1.0F));
                    }
                }
            }

            if (visibility == Toast.Visibility.SHOW && now - animationTime <= getAnimationTime()) {
                visibleTime = now;
            }

            float t = (float) (now - animationTime) / getAnimationTime();
            t = Math.min(1f, Math.max(0f, t));
            float ease = t * t;
            if (visibility == Toast.Visibility.HIDE) {
                ease = 1f - ease;
            }

            graphics.getMatrices().push();

            String anchor = config().getString("Toast-Rendering.Anchor-Point","bottom-left").toLowerCase(Locale.ROOT);
            float anchorX, anchorY;
            int toastW = config().getInt("Toast-Rendering.Width", 160);
            int toastH = config().getInt("Toast-Rendering.Height", 16);
            int screenH = minecraft.getWindow().getScaledHeight();

            // Calculate base position based on anchor
            switch (anchor) {
                case "top-left" -> {
                    anchorX = 0;
                    anchorY = 0;
                }
                case "top-center" -> {
                    anchorX = (screenWidth - toastW) / 2f;
                    anchorY = 0;
                }
                case "top-right" -> {
                    anchorX = screenWidth - toastW;
                    anchorY = 0;
                }
                case "middle-left" -> {
                    anchorX = 0;
                    anchorY = (screenH - toastH) / 2f;
                }
                case "middle-center", "center" -> {
                    anchorX = (screenWidth - toastW) / 2f;
                    anchorY = (screenH - toastH) / 2f;
                }
                case "middle-right" -> {
                    anchorX = screenWidth - toastW;
                    anchorY = (screenH - toastH) / 2f;
                }
                case "bottom-left" -> {
                    anchorX = 0;
                    anchorY = screenH - toastH;
                }
                case "bottom-center" -> {
                    anchorX = (screenWidth - toastW) / 2f;
                    anchorY = screenH - toastH;
                }
                case "bottom-right" -> {
                    anchorX = screenWidth - toastW;
                    anchorY = screenH - toastH;
                }
                default -> {
                    anchorX = screenWidth - toastW;
                    anchorY = screenH - toastH;
                }
            }

            // Apply offsets from config
            anchorX += getBaseX();
            anchorY += getBaseY();

            // Calculate target Y position for this toast based on index
            float targetX = anchorX;
            if (anchor.startsWith("top")) {
                targetY = anchorY + (index * toastH);
            } else if (anchor.startsWith("bottom")) {
                targetY = anchorY - (index * toastH);
            } else {
                // Middle anchors - stack down
                targetY = anchorY + (index * toastH);
            }

            // Initialize current position if needed
            if (currentY < 0) {
                currentY = targetY;
            }

            // Smoothly animate to new position when index changes
            if (Math.abs(currentY - targetY) > 0.1f) {
                // Animate at 20% of the distance per frame for smooth transition
                currentY += (targetY - currentY) * 0.2f;
            } else {
                currentY = targetY;
            }

            // Now apply animation effect (slide in/out)
            String dir = config().getString("Settings.Animation-Direction", "down").toLowerCase(Locale.ROOT);
            boolean noSlide = config().getBoolean("Toast-Animation.No-Slide", false);

            float x = targetX;
            float y = currentY;

            if (!noSlide) {
                float slideAmount = 1f - ease;

                switch (dir) {
                    case "left":
                        x -= slideAmount * toastW;
                        break;
                    case "right":
                        x += slideAmount * toastW;
                        break;
                    case "top":
                        y -= slideAmount * toastH;
                        break;
                    case "down":
                    case "bottom":
                    default:
                        y += slideAmount * toastH;
                        break;
                }
            }

            // Apply the calculated position
            graphics.getMatrices().translate(x, y, 800f);

            // Draw the toast
            Toast.Visibility newVis = toast.draw(graphics, null, now - visibleTime);

            graphics.getMatrices().pop();

            if (newVis != visibility) {
                animationTime = now - (long) ((1f - ease) * getAnimationTime());
                visibility = newVis;
                if (config().getBoolean("Sound-Settings.Enabled",true)) {
                    String dimensionId = minecraft.player.getWorld().getRegistryKey().getValue().toString();
                    String soundId = getSoundInForDimension(dimensionId);

                    try {
                        Identifier soundIdentifier = new Identifier(soundId);
                        SoundEvent soundEvent = SoundEvent.of(soundIdentifier);
                        minecraft.getSoundManager().play(PositionedSoundInstance.master(soundEvent, 1.0F));
                    } catch (Exception e) {
                        SkillExpNotifierFabric.LOGGER.error("Failed to play custom in sound: " + soundId, e);
                        minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_IN, 1.0F));
                    }
                }
            }

            boolean finished;
            if (toast instanceof XpToast xp) {
                finished = now - xp.getLastUpdateTime() > config().getLong("Toast-Animation.Stack-XP-Timer",5000);
            } else {
                finished = visibility == Toast.Visibility.HIDE && now - animationTime > getAnimationTime();
            }
            return finished;
        }
    }

    // The rest of your existing methods...
    private int getBaseX() {
        return config().getInt("Toast-Rendering.Base-X",-4);
    }

    private int getBaseY() {
        return config().getInt("Toast-Rendering.Base-Y",14);
    }

    public static String getSoundInForDimension(String dimId) {
        YamlConfigUtil config = ConfigHelper.getConfigUtil();
        List<String> mappings = config.getStringList("Sound-Settings.In-Sound", List.of(
                "minecraft:overworld=minecraft:ui.toast.in",
                "minecraft:the_nether=minecraft:ui.toast.in",
                "minecraft:the_end=minecraft:ui.toast.in"
        ));
        return parseMappings(mappings).getOrDefault(dimId, "minecraft:ui.toast.in");
    }

    public static String getSoundOutForDimension(String dimId) {
        YamlConfigUtil config = ConfigHelper.getConfigUtil();
        List<String> mappings = config.getStringList("Sound-Settings.Out-Sound", List.of(
                "minecraft:overworld=minecraft:ui.toast.out",
                "minecraft:the_nether=minecraft:ui.toast.out",
                "minecraft:the_end=minecraft:ui.toast.out"
        ));
        return parseMappings(mappings).getOrDefault(dimId, "minecraft:ui.toast.out");
    }

    private static Map<String, String> parseMappings(List<String> raw) {
        Map<String, String> map = new HashMap<>();
        for (String entry : raw) {
            var split = entry.split("=", 2);
            if (split.length == 2) {
                map.put(split[0].trim(), split[1].trim());
            }
        }
        return map;
    }
}