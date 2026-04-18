package org.CoreBytes.antimodel.client;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public final class AntiModelItemUtil {
    private AntiModelItemUtil() {
    }

    public static ItemStack withoutCustomModelData(ItemStack original) {
        if (original.isEmpty()) {
            return original;
        }

        ItemStack copy = original.copy();
        copy.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
        return copy;
    }
}

