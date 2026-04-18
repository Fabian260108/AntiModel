package org.CoreBytes.antimodel.client;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AntiModelKeyUtil {
    private AntiModelKeyUtil() {
    }

    /**
     * If the server provides a stable per-item ID via {@code minecraft:custom_data}, we use it.
     *
     * Supported layouts:
     * - {antimodel_id:"<uuid>"} (top-level)
     * - {antimodel:{id:"<uuid>"}} (namespaced compound)
     */
    public static String uidKey(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null || custom.isEmpty()) {
            return null;
        }

        NbtCompound nbt = custom.copyNbt();

        if (nbt.contains("antimodel_id")) {
            String id = nbt.getString("antimodel_id").orElse("");
            return id.isEmpty() ? null : "uid:" + id;
        }

        if (nbt.contains("antimodel")) {
            NbtCompound sub = nbt.getCompound("antimodel").orElse(null);
            if (sub != null && sub.contains("id")) {
                String id = sub.getString("id").orElse("");
                return id.isEmpty() ? null : "uid:" + id;
            }
        }

        return null;
    }

    /**
     * A stable key derived from the stack's component changes (not slot-based).
     *
     * This makes "disable this item" survive moves between slots and persists across restarts,
     * as long as the server keeps the same components (e.g. custom model data, custom name, lore).
     *
     * Note: If two stacks are identical in item+components, they will share the same signature key.
     */
    public static String signatureKey(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        // Avoid disabling "all dirt blocks" etc. Only items with changes should get a signature.
        if (stack.getComponentChanges().isEmpty()) {
            return null;
        }

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        String basis = itemId + "|" + stack.getComponentChanges();
        return "sig:" + sha256Hex(basis);
    }

    public static String keyForStackOrFallback(ItemStack stack, String fallbackKey) {
        String uid = uidKey(stack);
        if (uid != null) {
            return uid;
        }

        String sig = signatureKey(stack);
        if (sig != null) {
            return sig;
        }

        return fallbackKey;
    }

    private static String sha256Hex(String input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Should never happen on a normal JVM.
            throw new IllegalStateException(e);
        }

        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(Character.forDigit((b >>> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
