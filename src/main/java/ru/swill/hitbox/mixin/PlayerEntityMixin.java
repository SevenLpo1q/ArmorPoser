package ru.swill.hitbox.mixin;

import ru.swill.hitbox.HitboxState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void hitboxmod$scaleDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (!HitboxState.enabled) return;

        EntityDimensions original = cir.getReturnValue();
        if (original == null) return;

        cir.setReturnValue(original.scaled(HitboxState.scale));
    }
}
