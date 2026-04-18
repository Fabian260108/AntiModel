package org.CoreBytes.antimodel.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Client-only state: which stacks should render without custom models.
 *
 * Note: Without a stable per-stack ID from the server, we can only reliably target inventory slots.
 */
public final class AntiModelClientState {
    private static final AntiModelClientState INSTANCE = new AntiModelClientState();

    public static AntiModelClientState get() {
        return INSTANCE;
    }

    private final Set<String> disabledKeys = new HashSet<>();
    private AntiModelConfig config;

    private AntiModelClientState() {
    }

    public void init() {
        config = AntiModelConfig.load();
        disabledKeys.clear();
        disabledKeys.addAll(config.disabledKeys());
    }

    public boolean isDisabled(String key) {
        return disabledKeys.contains(key);
    }

    public boolean toggle(String key) {
        if (!disabledKeys.add(key)) {
            disabledKeys.remove(key);
            persist();
            return false;
        }
        persist();
        return true;
    }

    public Set<String> snapshot() {
        return Collections.unmodifiableSet(new HashSet<>(disabledKeys));
    }

    private void persist() {
        if (config == null) {
            config = AntiModelConfig.load();
        }
        config.setDisabledKeys(disabledKeys);
        config.save();
    }
}
