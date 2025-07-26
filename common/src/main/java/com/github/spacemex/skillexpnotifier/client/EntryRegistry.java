package com.github.spacemex.skillexpnotifier.client;

import com.github.spacemex.skillexpnotifier.Skillexpnotifier;
import com.github.spacemex.skillexpnotifier.config.ConfigReader;
import com.github.spacemex.skillexpnotifier.discovery.SearchForSkills;
import com.github.spacemex.yml.YamlConfigUtil;
import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class EntryRegistry {
    private record Entry(Pattern pattern, ItemStack icon){}
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static YamlConfigUtil config(){
        ConfigReader helper = new ConfigReader();
        return helper.getConfig();
    }

    public static void loadFromFile() {
        Path file = Skillexpnotifier.SKILLS_DIR;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            // Create parent directory if it doesn't exist
            Files.createDirectories(file.getParent());

            // 1) Auto-scan and overwrite JSON if enabled
            boolean addDefaults = false;
            if (config().getBoolean("Client-Settings.Auto-Register-Data", true)) {
                List<SearchForSkills.CategoryData> auto = SearchForSkills.scan();

                // Check if scan found any skills
                if (auto.isEmpty()) {
                    Skillexpnotifier.LOGGER.info("No skills found during scan, will use default entries");
                    addDefaults = true;
                } else {
                    JsonArray arr = new JsonArray();
                    for (var cd : auto) {
                        JsonObject o = new JsonObject();
                        // exact match on that category ID
                        o.addProperty("regex", "^" + Pattern.quote(cd.idPath()) + "$");
                        o.addProperty("icon", cd.iconId());
                        arr.add(o);
                    }
                    Files.writeString(file, gson.toJson(arr));
                }
            }

            // 2) If missing or scan found no skills, seed defaults
            if (!Files.exists(file) || addDefaults) {
                JsonArray defaults = new JsonArray();
                defaults.add(makeEntry(".*mining.*",     "minecraft:iron_pickaxe", gson));
                defaults.add(makeEntry(".*farming.*",    "minecraft:iron_hoe",    gson));
                defaults.add(makeEntry(".*husbandry.*",  "minecraft:wheat",       gson));
                defaults.add(makeEntry(".*fishing.*",    "minecraft:fishing_rod", gson));
                defaults.add(makeEntry(".*adventuring.*", "minecraft:iron_sword",  gson));
                // Add more general patterns
                defaults.add(makeEntry(".*magic.*",      "minecraft:enchanted_book", gson));
                defaults.add(makeEntry(".*combat.*",     "minecraft:diamond_sword", gson));
                defaults.add(makeEntry(".*crafting.*",   "minecraft:crafting_table", gson));
                defaults.add(makeEntry(".*brewing.*",    "minecraft:brewing_stand", gson));
                defaults.add(makeEntry(".*cooking.*",    "minecraft:furnace", gson));
                defaults.add(makeEntry(".*foraging.*",   "minecraft:stick", gson));
                defaults.add(makeEntry(".*smithing.*",   "minecraft:anvil", gson));

                Files.writeString(file, gson.toJson(defaults));
            }

            // 3) Read & populate ENTRIES
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonArray()) {
                    Skillexpnotifier.LOGGER.warn("IconMappings.json isn't an array, skipping.");
                    return;
                }

                ENTRIES.clear();
                JsonArray rootArray = root.getAsJsonArray();

                // Check if the array is empty after scanning, add defaults if needed
                if (rootArray.size() == 0) {
                    Skillexpnotifier.LOGGER.info("IconMappings.json is empty, adding default entries");
                    JsonArray defaults = new JsonArray();
                    defaults.add(makeEntry(".*mining.*",     "minecraft:iron_pickaxe", gson));
                    defaults.add(makeEntry(".*farming.*",    "minecraft:iron_hoe",    gson));
                    defaults.add(makeEntry(".*husbandry.*",  "minecraft:wheat",       gson));
                    defaults.add(makeEntry(".*fishing.*",    "minecraft:fishing_rod", gson));
                    defaults.add(makeEntry(".*adventuring.*", "minecraft:iron_sword",  gson));
                    defaults.add(makeEntry(".*magic.*",      "minecraft:enchanted_book", gson));
                    defaults.add(makeEntry(".*combat.*",     "minecraft:diamond_sword", gson));
                    defaults.add(makeEntry(".*crafting.*",   "minecraft:crafting_table", gson));

                    Files.writeString(file, gson.toJson(defaults));

                    // Re-read the file after writing defaults
                    try (Reader newReader = Files.newBufferedReader(file)) {
                        root = JsonParser.parseReader(newReader);
                        rootArray = root.getAsJsonArray();
                    }
                }

                for (JsonElement el : rootArray) {
                    JsonObject obj = el.getAsJsonObject();
                    String regex  = obj.get("regex").getAsString();
                    String iconId = obj.get("icon").getAsString();

                    Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(iconId));
                    ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
                    ENTRIES.add(new Entry(p, stack));
                }
            }

            Skillexpnotifier.LOGGER.info("Loaded {} icon mappings", ENTRIES.size());

        } catch (IOException e) {
            Skillexpnotifier.LOGGER.error("Failed loading IconMappings.json", e);
        }
    }

    private static JsonObject makeEntry(String regex, String icon, Gson g) {
        JsonObject o = new JsonObject();
        o.addProperty("regex", regex);
        o.addProperty("icon", icon);
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