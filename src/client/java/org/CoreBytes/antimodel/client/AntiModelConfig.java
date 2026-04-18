package org.CoreBytes.antimodel.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class AntiModelConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "antimodel.json";

    static AntiModelConfig load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            return new AntiModelConfig();
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            AntiModelConfig cfg = GSON.fromJson(reader, AntiModelConfig.class);
            return cfg != null ? cfg : new AntiModelConfig();
        } catch (IOException | JsonParseException e) {
            // Bad config should never prevent the client from launching.
            return new AntiModelConfig();
        }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private Map<String, OverrideEntry> overrides = new LinkedHashMap<>();
    // Legacy format compatibility for older config versions.
    private Set<String> disabledKeys = new LinkedHashSet<>();

    public Map<String, OverrideEntry> overrides() {
        if (overrides == null) {
            overrides = new LinkedHashMap<>();
        }
        return overrides;
    }

    public void setOverrides(Map<String, OverrideEntry> values) {
        overrides = new LinkedHashMap<>(values);
    }

    public Set<String> legacyDisabledKeys() {
        if (disabledKeys == null) {
            disabledKeys = new LinkedHashSet<>();
        }
        return disabledKeys;
    }

    public void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static final class OverrideEntry {
        private String type = "NONE";
        private int value = 0;

        public OverrideEntry() {
        }

        public OverrideEntry(String type, int value) {
            this.type = type;
            this.value = value;
        }

        public String type() {
            return type;
        }

        public int value() {
            return value;
        }
    }
}

