package org.CoreBytes.antimodel.client;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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

    public static String identityKey(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();

        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        CustomModelDataComponent cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        Identifier itemModel = stack.get(DataComponentTypes.ITEM_MODEL);
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);

        boolean hasIdentityData = customName != null
                || cmd != null
                || itemModel != null
                || (customData != null && !customData.isEmpty());

        if (!hasIdentityData) {
            return null;
        }

        String namePart = customName != null ? customName.getString() : "";
        String cmdPart = cmd != null ? cmd.toString() : "";
        String itemModelPart = itemModel != null ? itemModel.toString() : "";
        String customDataPart = customData != null ? customData.copyNbt().toString() : "";

        String basis = itemId + "|name=" + namePart + "|cmd=" + cmdPart + "|model=" + itemModelPart + "|data=" + customDataPart;
        return "itm:" + sha256Hex(basis);
    }

    public static String itemTypeKey(ItemStack stack) {
        if (stack.isEmpty()) {
            return "type:empty";
        }
        return "type:" + Registries.ITEM.getId(stack.getItem());
    }

    public static String keyForStackOrFallback(ItemStack stack, String fallbackKey) {
        String uid = uidKey(stack);
        if (uid != null) {
            return uid;
        }

        String identity = identityKey(stack);
        if (identity != null) {
            return identity;
        }

        String type = itemTypeKey(stack);
        if (type != null) {
            return type;
        }

        return fallbackKey != null ? fallbackKey : "fallback:empty";
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
