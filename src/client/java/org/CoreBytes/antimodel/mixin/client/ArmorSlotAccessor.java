package org.CoreBytes.antimodel.mixin.client;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.screen.slot.ArmorSlot")
public interface ArmorSlotAccessor {
    @Accessor("equipmentSlot")
    EquipmentSlot antimodel$getEquipmentSlot();

    @Accessor("entity")
    LivingEntity antimodel$getEntity();
}
