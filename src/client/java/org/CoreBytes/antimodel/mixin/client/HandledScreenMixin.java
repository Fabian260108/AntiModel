package org.CoreBytes.antimodel.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.CoreBytes.antimodel.client.AntiModelClientState;
import org.CoreBytes.antimodel.client.AntiModelKeyUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @ModifyVariable(
            method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"
            ),
            ordinal = 0
    )
    private ItemStack antimodel$stripCustomModelDataForPlayerSlots(ItemStack original, DrawContext context, Slot slot, int x, int y) {
        var client = MinecraftClient.getInstance();
        if (client.player == null || original.isEmpty()) {
            return original;
        }

        String key = null;
        String fallback = null;

        if (slot.inventory instanceof PlayerInventory) {
            fallback = "main:" + slot.getIndex();
            key = AntiModelKeyUtil.keyForStackOrFallback(original, fallback);
        } else if (slot instanceof ArmorSlotAccessor accessor) {
            if (accessor.antimodel$getEntity() == client.player) {
                fallback = "equip:" + accessor.antimodel$getEquipmentSlot().getName();
                key = AntiModelKeyUtil.keyForStackOrFallback(original, fallback);
            }
        }

        if (key != null) {
            return AntiModelClientState.get().apply(original, key, fallback);
        }

        return original;
    }
}
