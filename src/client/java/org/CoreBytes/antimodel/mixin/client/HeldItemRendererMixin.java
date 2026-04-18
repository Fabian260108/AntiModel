package org.CoreBytes.antimodel.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.CoreBytes.antimodel.client.AntiModelClientState;
import org.CoreBytes.antimodel.client.AntiModelItemUtil;
import org.CoreBytes.antimodel.client.AntiModelKeyUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @ModifyVariable(
            method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private ItemStack antimodel$stripCustomModelDataInHand(
            ItemStack original,
            AbstractClientPlayerEntity player,
            float tickDelta,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack stack,
            float equipProgress,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light
    ) {
        if (original.isEmpty()) {
            return original;
        }

        String key;
        if (hand == Hand.MAIN_HAND) {
            key = AntiModelKeyUtil.keyForStackOrFallback(original, "main:" + player.getInventory().getSelectedSlot());
        } else {
            key = AntiModelKeyUtil.keyForStackOrFallback(original, "main:" + PlayerInventory.OFF_HAND_SLOT);
        }

        if (AntiModelClientState.get().isDisabled(key)) {
            return AntiModelItemUtil.withoutCustomModelData(original);
        }

        return original;
    }
}
