package com.github.spacemex;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchForSkills {
    private static final Path GAME_DIR      = FabricLoader.getInstance().getGameDir();
    private static final Path KUBEJS_DIR    = GAME_DIR.resolve("kubejs");
    private static final Path KUBE_DATA_DIR = KUBEJS_DIR.resolve("data");
    private static final Path DATAPACK_DIR  = GAME_DIR.resolve("datapacks");

    /** A simple DTO for auto-discovered categories */
    public record CategoryData(String idPath, String iconId) {}

    public static List<CategoryData> scan() {
        List<File> found = new ArrayList<>();
        scanDir(DATAPACK_DIR, found);

        if (FabricLoader.getInstance().isModLoaded("kubejs")) {
            scanDir(KUBE_DATA_DIR, found);
        }

        var results = new ArrayList<CategoryData>();
        for (File file : found) {
            try (Reader r = Files.newBufferedReader(file.toPath())) {
                JsonObject obj = JsonParser.parseReader(r).getAsJsonObject();
                String idPath = file.getParentFile().getName();
                String itemId = obj.getAsJsonObject("icon")
                        .getAsJsonObject("data")
                        .get("item").getAsString();
                results.add(new CategoryData(idPath, itemId));
            } catch (Exception e) {
                SkillExpNotifierFabric.LOGGER.warn("Failed to parse {}", file.getPath(), e);
            }
        }
        return results;
    }

    private static void scanDir(Path dir, List<File> out) {
        File d = dir.toFile();
        if (!d.exists() || !d.isDirectory()) return;
        for (File f : Objects.requireNonNull(d.listFiles())) {
            if (f.isDirectory()) scanDir(f.toPath(), out);
            else if ("category.json".equals(f.getName())) out.add(f);
        }
    }
}
