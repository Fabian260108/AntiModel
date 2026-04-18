package org.CoreBytes.antimodel.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import org.CoreBytes.antimodel.client.AntiModelClientState;
import org.CoreBytes.antimodel.client.AntiModelItemUtil;
import org.CoreBytes.antimodel.client.AntiModelKeyUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @ModifyVariable(
            method = "renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private ItemStack antimodel$stripCustomModelDataInHudHotbar(ItemStack original) {
        var client = MinecraftClient.getInstance();
        if (client.player == null || original.isEmpty()) {
            return original;
        }

        // Identify the hotbar slot by identity; avoids breaking other stacks of same material.
        int slotIndex = -1;
        var inv = client.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i) == original) {
                slotIndex = i;
                break;
            }
        }

        if (slotIndex != -1) {
            String key = AntiModelKeyUtil.keyForStackOrFallback(original, "main:" + slotIndex);
            if (AntiModelClientState.get().isDisabled(key)) {
                return AntiModelItemUtil.withoutCustomModelData(original);
            }
        }

        return original;
    }
}
