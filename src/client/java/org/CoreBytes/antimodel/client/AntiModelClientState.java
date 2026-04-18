package org.CoreBytes.antimodel.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-only state: per-item visual overrides for custom model data.
 *
 * Note: Without a stable per-stack ID from the server, we can only reliably target inventory slots.
 */
public final class AntiModelClientState {
    private static final AntiModelClientState INSTANCE = new AntiModelClientState();

    public static AntiModelClientState get() {
        return INSTANCE;
    }

    private final Map<String, ModelOverride> overrides = new HashMap<>();
    private AntiModelConfig config;

    private AntiModelClientState() {
    }

    public void init() {
        config = AntiModelConfig.load();
        overrides.clear();

        for (var entry : config.overrides().entrySet()) {
            ModelOverride modelOverride = ModelOverride.fromConfig(entry.getValue());
            if (modelOverride != null) {
                overrides.put(entry.getKey(), modelOverride);
            }
        }

        // Backward compatibility: old "disabledKeys" means "remove CMD for render".
        for (String key : config.legacyDisabledKeys()) {
            overrides.putIfAbsent(key, ModelOverride.remove());
        }
    }

    public boolean isDisabled(String key) {
        return overrides.get(key) == ModelOverride.REMOVE;
    }

    public boolean hasOverride(String key) {
        return overrides.containsKey(key);
    }

    public String describe(String key) {
        ModelOverride modelOverride = overrides.get(key);
        if (modelOverride == null) {
            return "default";
        }
        if (modelOverride == ModelOverride.REMOVE) {
            return "hidden";
        }
        return "cmd=" + modelOverride.value;
    }

    public boolean toggle(String key) {
        if (!overrides.containsKey(key)) {
            overrides.put(key, ModelOverride.remove());
            persist();
            return true;
        }

        overrides.remove(key);
        persist();
        return false;
    }

    public void setCustomModelData(String key, int value) {
        overrides.put(key, ModelOverride.ofValue(value));
        persist();
    }

    public void clear(String key) {
        if (overrides.remove(key) != null) {
            persist();
        }
    }

    public net.minecraft.item.ItemStack apply(net.minecraft.item.ItemStack original, String key) {
        return apply(original, key, null);
    }

    public net.minecraft.item.ItemStack apply(net.minecraft.item.ItemStack original, String key, String fallbackKey) {
        ModelOverride modelOverride = overrides.get(key);
        if (modelOverride == null && fallbackKey != null) {
            modelOverride = overrides.get(fallbackKey);
        }
        if (modelOverride == null || original.isEmpty()) {
            return original;
        }

        if (modelOverride == ModelOverride.REMOVE) {
            return AntiModelItemUtil.withoutCustomModelData(original);
        }

        return AntiModelItemUtil.withCustomModelData(original, modelOverride.value);
    }

    public Map<String, String> snapshot() {
        Map<String, String> result = new HashMap<>();
        for (var entry : overrides.entrySet()) {
            result.put(entry.getKey(), describe(entry.getKey()));
        }
        return Collections.unmodifiableMap(result);
    }

    private void persist() {
        if (config == null) {
            config = AntiModelConfig.load();
        }

        Map<String, AntiModelConfig.OverrideEntry> data = new HashMap<>();
        for (var entry : overrides.entrySet()) {
            data.put(entry.getKey(), entry.getValue().toConfig());
        }

        config.setOverrides(data);
        config.save();
    }

    private static final class ModelOverride {
        private static final ModelOverride REMOVE = new ModelOverride(true, 0);

        private final boolean remove;
        private final int value;

        private ModelOverride(boolean remove, int value) {
            this.remove = remove;
            this.value = value;
        }

        static ModelOverride remove() {
            return REMOVE;
        }

        static ModelOverride ofValue(int value) {
            return new ModelOverride(false, value);
        }

        static ModelOverride fromConfig(AntiModelConfig.OverrideEntry entry) {
            if (entry == null) {
                return null;
            }
            if ("REMOVE".equalsIgnoreCase(entry.type())) {
                return REMOVE;
            }
            if ("VALUE".equalsIgnoreCase(entry.type())) {
                return ofValue(entry.value());
            }
            return null;
        }

        AntiModelConfig.OverrideEntry toConfig() {
            if (remove) {
                return new AntiModelConfig.OverrideEntry("REMOVE", 0);
            }
            return new AntiModelConfig.OverrideEntry("VALUE", value);
        }
    }
}
