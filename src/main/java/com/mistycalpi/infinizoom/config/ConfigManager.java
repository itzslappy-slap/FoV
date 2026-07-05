package com.mistycalpi.infinizoom.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("infinizoom.json");

    private static ZoomConfig config = new ZoomConfig();

    private ConfigManager() {}

    public static ZoomConfig get() {
        return config;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ZoomConfig loaded = GSON.fromJson(json, ZoomConfig.class);
                if (loaded != null) {
                    loaded.clampToValidRanges();
                    config = loaded;
                }
            } catch (Exception e) {
                System.err.println("[InfiniZoom] Failed to read config, using defaults: " + e);
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            System.err.println("[InfiniZoom] Failed to save config: " + e);
        }
    }

    public static void resetToDefaults() {
        config.copyFrom(new ZoomConfig());
        save();
    }
}
