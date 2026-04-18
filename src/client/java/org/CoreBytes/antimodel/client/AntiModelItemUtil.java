package org.CoreBytes.antimodel.client;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class AntiModelItemUtil {
    private AntiModelItemUtil() {
    }

    public static ItemStack withoutItemModel(ItemStack original) {
        if (original.isEmpty()) {
            return original;
        }

        ItemStack copy = original.copy();
        copy.remove(DataComponentTypes.ITEM_MODEL);
        return copy;
    }

    public static ItemStack withoutCustomModelData(ItemStack original) {
        if (original.isEmpty()) {
            return original;
        }

        ItemStack copy = original.copy();
        copy.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
        return copy;
    }

    public static ItemStack withCustomModelData(ItemStack original, int customModelData) {
        if (original.isEmpty()) {
            return original;
        }

        ItemStack copy = original.copy();
        CustomModelDataComponent existing = copy.get(DataComponentTypes.CUSTOM_MODEL_DATA);

        List<Float> floats = new ArrayList<>(existing != null ? existing.floats() : List.of());
        List<Boolean> flags = new ArrayList<>(existing != null ? existing.flags() : List.of());
        List<String> strings = new ArrayList<>(existing != null ? existing.strings() : List.of());
        List<Integer> colors = new ArrayList<>(existing != null ? existing.colors() : List.of());

        if (floats.isEmpty()) {
            floats.add((float) customModelData);
        } else {
            floats.set(0, (float) customModelData);
        }

        copy.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(
                floats,
                flags,
                strings,
                colors
        ));
        return copy;
    }
}

