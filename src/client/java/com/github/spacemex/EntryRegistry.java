package com.github.spacemex;

import com.github.spacemex.yml.YamlConfigUtil;
import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
    private static YamlConfigUtil config(){
        ConfigHelper helper = new ConfigHelper();
        return helper.getConfigUtil();
    }

    public static void loadFromFile() {
        Path file = Path.of("config/SkillExpNotifier/IconMappings.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            // First check if file exists
            boolean fileExists = Files.exists(file);

            // Create parent directories if needed
            Files.createDirectories(file.getParent());

            // Prepare auto-generated data if enabled
            JsonArray autoEntries = new JsonArray();
            if (config().getBoolean("Client-Settings.Auto-Register-Data", true)) {
                List<SearchForSkills.CategoryData> auto = SearchForSkills.scan();
                for (var cd : auto) {
                    JsonObject o = new JsonObject();
                    o.addProperty("regex", "^" + Pattern.quote(cd.idPath()) + "$");
                    o.addProperty("icon", cd.iconId());
                    autoEntries.add(o);
                }
            }

            // If file doesn't exist or auto-entries is empty, use defaults
            if (!fileExists || (autoEntries.size() == 0 && config().getBoolean("Client-Settings.Auto-Register-Data", true))) {
                JsonArray defaults = new JsonArray();
                defaults.add(makeEntry(".*mining.*",     "minecraft:iron_pickaxe", gson));
                defaults.add(makeEntry(".*farming.*",    "minecraft:iron_hoe",     gson));
                defaults.add(makeEntry(".*husbandry.*",  "minecraft:wheat",        gson));
                defaults.add(makeEntry(".*fishing.*",    "minecraft:fishing_rod",  gson));
                defaults.add(makeEntry(".*adventuring.*","minecraft:iron_sword",   gson));

                // If we have auto entries, add them to defaults
                if (autoEntries.size() > 0) {
                    for (int i = 0; i < autoEntries.size(); i++) {
                        defaults.add(autoEntries.get(i));
                    }
                }

                Files.writeString(file, gson.toJson(defaults));
            } else if (autoEntries.size() > 0) {
                // Only write auto entries if we found some and the file exists
                Files.writeString(file, gson.toJson(autoEntries));
            }

            // Read the file and populate entries
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonArray()) {
                    SkillExpNotifierFabric.LOGGER.warn("IconMappings.json isn't an array, skipping.");
                    return;
                }
                ENTRIES.clear();
                for (JsonElement el : root.getAsJsonArray()) {
                    JsonObject obj = el.getAsJsonObject();
                    String regex = obj.get("regex").getAsString();
                    String iconId = obj.get("icon").getAsString();

                    Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    Item item = Registries.ITEM.get(new Identifier(iconId));
                    ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
                    ENTRIES.add(new Entry(p, stack));
                }
            }
        } catch (IOException e) {
            SkillExpNotifierFabric.LOGGER.error("Failed loading IconMappings.json", e);
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
