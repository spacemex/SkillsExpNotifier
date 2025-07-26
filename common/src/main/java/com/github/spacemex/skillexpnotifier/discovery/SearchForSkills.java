package com.github.spacemex.skillexpnotifier.discovery;

import com.github.spacemex.skillexpnotifier.Skillexpnotifier;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchForSkills {
    public record CategoryData(String idPath,String iconId){}

    public static List<CategoryData> scan(){
        List<File> found = new ArrayList<>();
        Path datapack = Skillexpnotifier.GAME_DIR.resolve("datapacks");
        scanDir(datapack,found);

        Path kubeJS = Skillexpnotifier.GAME_DIR.resolve("kubejs");
        if (kubeJS.isAbsolute()){
            Skillexpnotifier.LOGGER.info("Found KubeJS, Scanning for data");
            Path kubeData = kubeJS.resolve("data");
            scanDir(kubeData,found);
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
                Skillexpnotifier.LOGGER.warn("Failed to parse {}", file.getPath(), e);
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
