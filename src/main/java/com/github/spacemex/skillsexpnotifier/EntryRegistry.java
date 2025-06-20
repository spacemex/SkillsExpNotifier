package com.github.spacemex.skillsexpnotifier;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EntryRegistry {
    private record Entry(Pattern pattern, ItemStack icon) {}
    private static final List<Entry> ENTRIES = new ArrayList<>();

    public static void loadFromFile(Path configDir) {
        Path file = configDir.resolve("SkillExpNotifier/IconMappings.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());

                JsonArray defaults = new JsonArray();
                defaults.add(makeEntry(".*mining.*",       "minecraft:iron_pickaxe", gson));
                defaults.add(makeEntry(".*farming.*",       "minecraft:iron_hoe",    gson));
                defaults.add(makeEntry(".*husbandry.*",     "minecraft:wheat",       gson));
                defaults.add(makeEntry(".*fishing.*",       "minecraft:fishing_rod", gson));
                defaults.add(makeEntry(".*adventuring.*",   "minecraft:iron_sword",  gson));

                // write it out
                Files.writeString(file, gson.toJson(defaults));
            }

            // now read & parse
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
                ENTRIES.clear();
                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    String regex = obj.get("regex").getAsString();
                    String iconId = obj.get("icon").getAsString();

                    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    Item item = ForgeRegistries.ITEMS.getValue( ResourceLocation.parse(iconId));
                    ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);

                    ENTRIES.add(new Entry(pattern, stack));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonObject makeEntry(String regex, String icon, Gson gson) {
        JsonObject o = new JsonObject();
        o.addProperty("regex", regex);
        o.addProperty("icon",   icon);
        return o;
    }

    /**
     * Given a category ID path (e.g. "mining_skills"), returns the first matching icon,
     * or EMPTY if none matched.
     */
    public static ItemStack getIconFor(String categoryPath) {
        for (var entry : ENTRIES) {
            if (entry.pattern.matcher(categoryPath).matches()) {
                return entry.icon.copy();
            }
        }
        return ItemStack.EMPTY;
    }
}
