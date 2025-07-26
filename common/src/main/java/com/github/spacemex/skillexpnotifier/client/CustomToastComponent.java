package com.github.spacemex.skillexpnotifier.client;

import com.github.spacemex.skillexpnotifier.Skillexpnotifier;
import com.github.spacemex.skillexpnotifier.config.ConfigReader;
import com.github.spacemex.yml.YamlConfigUtil;
import com.google.common.collect.Queues;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.*;

@Environment(EnvType.CLIENT)
public class CustomToastComponent {
    private final Minecraft minecraft;
    private final List<CustomToastInstance<?>> visible = new ArrayList<>();
    private final BitSet occupiedSlots = new BitSet();
    private final Deque<Toast> queued = Queues.newArrayDeque();

    public CustomToastComponent(Minecraft minecraft) {
        this.minecraft = Objects.requireNonNull(minecraft);
    }

    private static YamlConfigUtil config(){
        ConfigReader helper = new ConfigReader();
        return helper.getConfig();
    }

    /**
     * Queue a toast for display
     */
    public void addToast(Toast toast) {
        if (config().getBoolean("Client-Settings.Disable",false)) return;
        // 1) try to merge into any *visible* toast with the same token
        XpToast existingVisible = getToast(XpToast.class, toast.getToken());
        if (existingVisible != null && toast instanceof XpToast incoming) {
            existingVisible.addGained(incoming.getGained());
            Skillexpnotifier.LOGGER.debug("Merged Visible toast {} and {}", existingVisible, toast);
            return;
        }

        // 2) try to merge into any *queued* toast with the same token
        for (Toast queuedToast : queued) {
            if (queuedToast instanceof XpToast queuedXp &&
                    queuedToast.getClass() == toast.getClass() &&
                    Objects.equals(queuedToast.getToken(), toast.getToken())) {
                queuedXp.addGained(((XpToast) toast).getGained());
                Skillexpnotifier.LOGGER.debug("Merged toasts {} and {}", queuedToast, toast);
                return;
            }
        }
        // 3) otherwise, enqueue a brand‐new one
        queued.add(toast);
        Skillexpnotifier.LOGGER.debug("Queued toast {}", toast);
    }

    /**
     * Render all toasts at the custom position.
     */
    public void render(GuiGraphics graphics) {
        if (minecraft.options.hideGui) return;

        int screenWidth = graphics.guiWidth();

        // remove finished
        Iterator<CustomToastInstance<?>> it = visible.iterator();
        while (it.hasNext()) {
            CustomToastInstance<?> instance = it.next();
            if (instance.render(screenWidth, graphics)) {
                occupiedSlots.clear(instance.index, instance.index + instance.slotCount);
                it.remove();
            }
        }

        // queue new
        if (!queued.isEmpty() && freeSlots() > 0) {
            Iterator<Toast> qi = queued.iterator();
            while (qi.hasNext() && freeSlots() > 0) {
                Toast toast = qi.next();
                int slots = toast.slotCount();
                int idx = findFreeIndex(slots);
                if (idx != -1) {
                    visible.add(new CustomToastInstance<>(toast, idx, slots));
                    occupiedSlots.set(idx, idx + slots);
                    qi.remove();
                }
            }
        }
    }

    private int freeSlots() {
        return getSlotCount() - occupiedSlots.cardinality();
    }

    private int findFreeIndex(int slotCount) {
        if (freeSlots() < slotCount) return -1;
        int count = 0;
        for (int i = 0; i < getSlotCount(); i++) {
            if (occupiedSlots.get(i)) {
                count = 0;
            } else {
                if (++count == slotCount) {
                    return i + 1 - slotCount;
                }
            }
        }
        return -1;
    }

    /**
     * @param pToastClass the Toast subclass
     * @param token       what getToken() must match
     * @return a live toast instance, or null
     */
    @SuppressWarnings("unchecked")
    public <T extends Toast> T getToast(Class<? extends T> pToastClass, Object token) {
        // 1) look in visible list
        for (CustomToastInstance<?> inst : visible) {
            Toast t = inst.toast;
            if (pToastClass.isAssignableFrom(t.getClass()) && t.getToken().equals(token))
                return (T) t;
        }
        // 2) look in queued
        for (Toast t : queued) {
            if (pToastClass.isAssignableFrom(t.getClass()) && t.getToken().equals(token))
                return (T) t;
        }
        return null;
    }

    @Environment(EnvType.CLIENT)
    private class CustomToastInstance<T extends Toast> {
        private final T toast;
        private final int index;
        private final int slotCount;
        private long animationTime = -1L;
        private long visibleTime = -1L;
        private Toast.Visibility visibility = Toast.Visibility.SHOW;

        CustomToastInstance(T toast, int index, int slotCount) {
            this.toast = toast;
            this.index = index;
            this.slotCount = slotCount;

            if (toast instanceof XpToast xpToast){
                xpToast.addGained(0);
            }
        }

        /**
         * Renders this toast instance at a custom anchor point.
         * <p>
         * Anchor reference:
         * • X axis (horizontal):
         * – getBaseX() is your configured “base” offset from the left edge.
         * – 0 = left edge of the screen; screenWidth = right edge.
         * – slideFromLeft true → toast slides in from the left; false → from the right.
         * • Y axis (vertical):
         * – getBaseY() is your configured “base” offset from the top.
         * – 0 = very top; screenHeight = bottom of the window.
         * – stackTopDown true → each toast moves downward by index×height;
         * false → stacks upward.
         * • Z axis (depth):
         * – 800.0f fixes the toast in front of almost all other GUI elements
         * (higher Z draws “closer” to the camera in Minecraft’s UI).
         * <p>
         * Depending on whether you’ve chosen a horizontal or vertical slide, we
         * offset by (1-ease)×width or (1-ease)×height to animate the pop-in.
         */
        public boolean render(int screenWidth, GuiGraphics graphics) {
            long now = Util.getMillis();
            if (animationTime < 0) {
                animationTime = now;
                if (config().getBoolean("Sound-Settings.Enabled",true)){
                    String dimKey = minecraft.level.dimension().location().toString();
                    if (dimKey.isEmpty()) dimKey = "minecraft:overworld";
                    SoundEvent inSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse(getSoundInForDimension(dimKey)));
                    if (inSound != null){
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(inSound, 1f,1f));
                    }else {
                        Skillexpnotifier.LOGGER.warn("Sound For Toast In Not Found: {} Using Default Sound For Toast In",
                                getSoundInForDimension(dimKey));
                        visibility.playSound(minecraft.getSoundManager());
                    }
                }
            }
            //Keeps Toast On Screen When Updating
            if (visibility == Toast.Visibility.SHOW && now - animationTime <= getAnimationTime()) {
                visibleTime = now;
            }

            float t = (float) (now - animationTime) / getAnimationTime();
            t = Math.min(1f, Math.max(0f, t));
            float ease = t * t;
            if (visibility == Toast.Visibility.HIDE) {
                ease = 1f - ease;
            }

            graphics.pose().pushPose();

            // --- custom anchor/slide logic start ---
            // compute static anchor based on config
            String anchor = config().getString("Toast-Rendering.Anchor-Point", "bottom-left").toLowerCase();
            float anchorX, anchorY;
            int toastW = toast.width(), toastH = toast.height();
            int screenH = graphics.guiHeight();

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
                case "middle-center",
                     "center" -> {
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
                default -> {
                    anchorX = screenWidth - toastW;
                    anchorY = screenH - toastH;
                }
            }

            // Add Back The Offsets :P
            anchorX += getBaseX();
            anchorY += getBaseY();

            // determine slide/stack direction
            String dir = config().getString("Settings.Animation-Direction", "top").toLowerCase();
            boolean isLeft = dir.equals("left");
            boolean isRight = dir.equals("right");
            boolean isTop = dir.equals("top");
            boolean isDown = dir.equals("down");
            boolean noSlide = config().getBoolean("Toast-Animation.No-Slide",false);

            boolean slideH = !noSlide && (isLeft || isRight);
            boolean slideV = !noSlide && (isTop || isDown);
            boolean slideFromLeft = isLeft && !isRight;
            boolean stackTopDown = isTop && !isDown;

            float slideX = (1f - ease) * toastW;
            float slideY = (1f - ease) * toastH;

            float x, y;
            if (slideH) {
                x = slideFromLeft
                        ? anchorX - slideX
                        : anchorX + slideX;
                y = anchorY + (stackTopDown
                        ? index * toastH
                        : -index * toastH);
            } else if (slideV) {
                x = anchorX + (stackTopDown
                        ? index * toastW
                        : -index * toastW);
                y = isTop
                        ? anchorY - slideY
                        : anchorY + slideY;
            } else {
                // vanilla fallback: slide from right, stack down
                x = anchorX;
                y = anchorY + index * toastH;
            }
            graphics.pose().translate(x, y, 800f);
            // --- custom anchor/slide logic end ---

            Toast.Visibility newVis = toast.render(graphics, null, now - visibleTime);
            graphics.pose().popPose();

            if (newVis != visibility) {
                animationTime = now - (long) ((1f - ease) * getAnimationTime());
                visibility = newVis;
                if (config().getBoolean("Sound-Settings.Enabled",true)){
                    String dimKey = minecraft.level.dimension().location().toString();
                    if (dimKey.isEmpty()) dimKey = "minecraft:overworld";
                    SoundEvent outSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse(getSoundOutForDimension(dimKey)));
                    if (outSound != null){
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(outSound, 1f,1f));
                    }else {
                        Skillexpnotifier.LOGGER.warn("Sound For Toast Out Not Found: {} Using Default Sound For Toast Out",
                                getSoundOutForDimension(dimKey));
                        visibility.playSound(minecraft.getSoundManager());
                    }
                }
            }

            boolean finished;
            if (toast instanceof XpToast xp){
                finished = now - xp.getLastUpdateTime() > config().getLong("Toast-Animation.Stack-XP-Timer",5000);
            }else {
                finished = visibility == Toast.Visibility.HIDE && now - animationTime > getAnimationTime();
            }
            return finished;
        }
    }

    private int getBaseX(){
        return config().getInt("Toast-Rendering.Base-X",0);
    }
    private int getBaseY(){
        return config().getInt("Toast-Rendering.Base-Y",0);
    }

    private int getSlotCount(){
        return Math.max(1,config().getInt("Settings.Max-Toasts",1));
    }

    private long getAnimationTime(){
        return config().getLong("Toast-Animation.Animation-Time",1000) <= 0
                ? 6000L :config().getLong("Toast-Animation.Animation-Time",1000);
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
        Map<String,String> m = parseMappings((List<String>) config().getStringList("Sound-Settings.In-Sound",new ArrayList<>()));
        return m.getOrDefault(dimId, "minecraft:ui.toast.in");
    }

    public static String getSoundOutForDimension(String dimId) {
        Map<String,String> m = parseMappings((List<String>) config().getStringList("Sound-Settings.Out-Sound",new ArrayList<>()));
        return m.getOrDefault(dimId, "minecraft:ui.toast.out");
    }


}
