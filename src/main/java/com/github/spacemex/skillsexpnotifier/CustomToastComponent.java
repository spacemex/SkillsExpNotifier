package com.github.spacemex.skillsexpnotifier;

import com.google.common.collect.Queues;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class CustomToastComponent {

    private final Minecraft minecraft;
    private final List<CustomToastInstance<?>> visible = new ArrayList<>();
    private final BitSet occupiedSlots = new BitSet();
    private final Deque<Toast> queued = Queues.newArrayDeque();

    /**
     * @param minecraft The game instance
     */
    public CustomToastComponent(Minecraft minecraft) {
        this.minecraft = Objects.requireNonNull(minecraft);
    }

    /**
     * Queue a toast for display
     */
    public void addToast(Toast toast) {
        if (net.minecraftforge.client.ForgeHooksClient.onToastAdd(toast)) return;
        queued.add(toast);
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

    @OnlyIn(Dist.CLIENT)
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
        }

        /**
         * @return true if this toast is fully finished and can be removed
         */
        public boolean render(int screenWidth, GuiGraphics graphics) {
            long now = Util.getMillis();
            if (animationTime < 0) {
                animationTime = now;
                visibility.playSound(minecraft.getSoundManager());
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

            graphics.pose().pushPose();
            if (determineDirection()) {
                // slide in from the left instead of the right
                graphics.pose().translate(
                        getBaseX() - (1f - ease) * toast.width(),
                        getBaseY() + index * toast.height(),
                        800f
                );
            }else {
                // slide in from the right instead of the left
                graphics.pose().translate(
                        getBaseX() + (1f - ease) * toast.width(),
                        getBaseY() + index * toast.height(),
                        800f
                );
            }

            Toast.Visibility newVis = toast.render(graphics, null, now - visibleTime);
            graphics.pose().popPose();

            if (newVis != visibility) {
                animationTime = now - (long) ((1f - ease) * getAnimationTime());
                visibility = newVis;
                visibility.playSound(minecraft.getSoundManager());
            }

            return visibility == Toast.Visibility.HIDE && now - animationTime > getAnimationTime();
        }
    }

    /**
     *
     * @return true if left else false
     */
    private boolean determineDirection(){
        return getDirection().equals("left");
    }

    private int getBaseX(){
        return Config.X_OFFSET.get();
    }
    private int getBaseY(){
        return Config.Y_OFFSET.get();
    }
    private String getDirection(){
        return Config.ANIMATION_DIRECTION.get().toString().toLowerCase();
    }

    private int getSlotCount(){
        return Config.MAX_TOASTS.get() < 1 ? 1 : Config.MAX_TOASTS.get();
    }

    private long getAnimationTime(){
        return Config.ANIMATION_TIME.get() <= 0 ? 600L : Config.ANIMATION_TIME.get();
    }
}
