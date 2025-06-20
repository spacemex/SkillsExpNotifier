package com.github.spacemex.skillsexpnotifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
@Deprecated
public class IconRegistry {
    private record Entry(Pattern regex, Item icon) {}

    /**
     * Till I'm able to gain access to the icon and title, it has to be hardcoded
     * by default, I tailored it to a modpack called Society:Sunlit Valley
     * <p>
     * Uses regx matching to try to determine the title of the skill and assigns an icon to it
     */
    private static final List<Entry> ICONS = new ArrayList<>(List.of(
            new Entry(Pattern.compile(".*mining.*", Pattern.CASE_INSENSITIVE), Items.IRON_PICKAXE),
            new Entry(Pattern.compile(".*farming*.",Pattern.CASE_INSENSITIVE), Items.IRON_HOE),
            new Entry(Pattern.compile(".*husbandry*.",Pattern.CASE_INSENSITIVE), Items.WHEAT),
            new Entry(Pattern.compile(".*fishing*.",Pattern.CASE_INSENSITIVE), Items.FISHING_ROD),
            new Entry(Pattern.compile(".*adventuring*.",Pattern.CASE_INSENSITIVE),Items.IRON_SWORD)
    ));

    /**
     * Added For Kubjs Users
     * @param regex regex Pattern
     * @param icon Item
     */
    public static void addEntry(Pattern regex, Item icon) {
        ICONS.add(new Entry(regex, icon));
    }

    /**
     * Returns a 1-count ItemStack to use as the icon for this skill/category ID.
     * Falls back to a book if nothing matches.
     */
    public static ItemStack getIconFor(ResourceLocation id) {
        String path = id.getPath();
        for (var entry : ICONS) {
            if (entry.regex.matcher(path).matches()) {
                return new ItemStack(entry.icon);
            }
        }
        return new ItemStack(Items.BARRIER);
    }
}
