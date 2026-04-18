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
import java.util.Collection;
import java.util.LinkedHashSet;
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

    private Set<String> disabledKeys = new LinkedHashSet<>();

    public Set<String> disabledKeys() {
        return disabledKeys;
    }

    public void setDisabledKeys(Collection<String> keys) {
        disabledKeys = new LinkedHashSet<>(keys);
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
}

