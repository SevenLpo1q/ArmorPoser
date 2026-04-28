package moonulio.skinpresets.client.mixin;

import moonulio.skinpresets.client.gui.SkinPresetsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void skinPresets$addButton(CallbackInfo ci) {
        int x = this.width - 126;
        int y = 8;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Skin Presets"), button -> {
            this.client.setScreen(new SkinPresetsScreen((MultiplayerScreen) (Object) this));
        }).dimensions(x, y, 118, 20).build());
    }
}
