package org.CoreBytes.antimodel.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.CoreBytes.antimodel.client.AntiModelClientState;
import org.CoreBytes.antimodel.client.AntiModelItemUtil;
import org.CoreBytes.antimodel.client.AntiModelKeyUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V",
            at = @At("HEAD")
    )
    private void antimodel$stripCustomModelDataThirdPersonArmor(
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            BipedEntityRenderState state,
            float limbAngle,
            float limbDistance,
            CallbackInfo ci
    ) {
        var client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        if (!(state instanceof PlayerEntityRenderState playerState) || playerState.id != client.player.getId()) {
            return;
        }

        if (isDisabled(playerState.equippedHeadStack, "equip:head")) {
            playerState.equippedHeadStack = AntiModelItemUtil.withoutCustomModelData(playerState.equippedHeadStack);
        }
        if (isDisabled(playerState.equippedChestStack, "equip:chest")) {
            playerState.equippedChestStack = AntiModelItemUtil.withoutCustomModelData(playerState.equippedChestStack);
        }
        if (isDisabled(playerState.equippedLegsStack, "equip:legs")) {
            playerState.equippedLegsStack = AntiModelItemUtil.withoutCustomModelData(playerState.equippedLegsStack);
        }
        if (isDisabled(playerState.equippedFeetStack, "equip:feet")) {
            playerState.equippedFeetStack = AntiModelItemUtil.withoutCustomModelData(playerState.equippedFeetStack);
        }
    }

    private static boolean isDisabled(net.minecraft.item.ItemStack stack, String fallbackKey) {
        String key = AntiModelKeyUtil.keyForStackOrFallback(stack, fallbackKey);
        return AntiModelClientState.get().isDisabled(key);
    }
}
