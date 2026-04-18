package org.CoreBytes.antimodel.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.CoreBytes.antimodel.client.AntiModelClientState;
import org.CoreBytes.antimodel.client.AntiModelItemUtil;
import org.CoreBytes.antimodel.client.AntiModelKeyUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererMixin {
    @ModifyVariable(
            method = "renderItem(Lnet/minecraft/client/render/entity/state/ArmedEntityRenderState;Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            index = 2
    )
    private ItemStack antimodel$stripCustomModelDataThirdPersonHeld(
            ItemStack original,
            ArmedEntityRenderState state,
            ItemRenderState itemState,
            Arm arm,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light
    ) {
        var client = MinecraftClient.getInstance();
        if (client.player == null || original.isEmpty()) {
            return original;
        }

        if (!(state instanceof PlayerEntityRenderState playerState) || playerState.id != client.player.getId()) {
            return original;
        }

        String fallbackKey = (arm == state.mainArm)
                ? ("main:" + client.player.getInventory().getSelectedSlot())
                : ("main:" + PlayerInventory.OFF_HAND_SLOT);
        String key = AntiModelKeyUtil.keyForStackOrFallback(original, fallbackKey);

        if (AntiModelClientState.get().isDisabled(key)) {
            return AntiModelItemUtil.withoutCustomModelData(original);
        }

        return original;
    }
}

