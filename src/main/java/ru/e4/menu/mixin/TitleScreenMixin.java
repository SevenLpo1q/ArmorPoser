package ru.e4.menu.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    private static final Identifier E4_BACKGROUND = Identifier.of("e4menu", "textures/gui/main_menu_background.png");

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void e4menu$replacePanorama(DrawContext context, float delta, CallbackInfo ci) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        context.drawTexture(E4_BACKGROUND, 0, 0, 0, 0, width, height, width, height);
        ci.cancel();
    }
}
