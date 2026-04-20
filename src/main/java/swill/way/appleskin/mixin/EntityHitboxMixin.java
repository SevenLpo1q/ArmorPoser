package swill.way.appleskin.mixin;

import swill.way.appleskin.AppleSkinClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityHitboxMixin {
    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void appleskin$expandPlayerHitbox(CallbackInfoReturnable<Box> cir) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof PlayerEntity) || entity.isMainPlayer()) {
            return;
        }

        double extra = AppleSkinClient.getExtraHitbox();
        if (extra <= 0.0D) {
            return;
        }

        Box original = cir.getReturnValue();
        cir.setReturnValue(original.expand(extra));
    }
}
