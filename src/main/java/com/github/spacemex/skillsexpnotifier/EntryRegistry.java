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
import java.util.*;
import java.util.regex.Pattern;

public class EntryRegistry {
    private record Entry(Pattern pattern, ItemStack icon) {}
    private static final List<Entry> ENTRIES = new ArrayList<>();

    public static void loadFromFile(Path configDir) {
        Path file = configDir.resolve("SkillExpNotifier/IconMappings.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            // 1) Auto-scan & overwrite JSON if enabled
            if (Config.AUTOREGISTER_DATA.get()) {
                List<SearchForSkills.CategoryData> auto = SearchForSkills.scan();
                Files.createDirectories(file.getParent());
                JsonArray arr = new JsonArray();
                for (var cd : auto) {
                    JsonObject o = new JsonObject();
                    // exact match on that category ID
                    o.addProperty("regex", "^" + Pattern.quote(cd.idPath()) + "$");
                    o.addProperty("icon",   cd.iconId());
                    arr.add(o);
                }
                Files.writeString(file, gson.toJson(arr));
            }

            // 2) If missing, seed defaults
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                JsonArray defaults = new JsonArray();
                defaults.add(makeEntry(".*mining.*",     "minecraft:iron_pickaxe", gson));
                defaults.add(makeEntry(".*farming.*",     "minecraft:iron_hoe",    gson));
                defaults.add(makeEntry(".*husbandry.*",   "minecraft:wheat",       gson));
                defaults.add(makeEntry(".*fishing.*",     "minecraft:fishing_rod", gson));
                defaults.add(makeEntry(".*adventuring.*", "minecraft:iron_sword",  gson));
                Files.writeString(file, gson.toJson(defaults));
            }

            // 3) Read & populate ENTRIES
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonArray()) {
                    Skillsexpnotifier.LOGGER.warn("IconMappings.json isn’t an array, skipping.");
                    return;
                }
                ENTRIES.clear();
                for (JsonElement el : root.getAsJsonArray()) {
                    JsonObject obj = el.getAsJsonObject();
                    String regex  = obj.get("regex").getAsString();
                    String iconId = obj.get("icon").getAsString();

                    Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    Item item  = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(iconId));
                    ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
                    ENTRIES.add(new Entry(p, stack));
                }
            }
        } catch (IOException e) {
            Skillsexpnotifier.LOGGER.error("Failed loading IconMappings.json", e);
        }
    }

    private static JsonObject makeEntry(String regex, String icon, Gson g) {
        JsonObject o = new JsonObject();
        o.addProperty("regex", regex);
        o.addProperty("icon",   icon);
        return o;
    }

    public static ItemStack getIconFor(String categoryPath) {
        for (var e : ENTRIES) {
            if (e.pattern.matcher(categoryPath).matches()) {
                return e.icon.copy();
            }
        }
        return ItemStack.EMPTY;
    }
}
