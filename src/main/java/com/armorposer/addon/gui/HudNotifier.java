package com.armorposer.addon.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class HudNotifier {
    private static long shownAt = -1L;
    private static final long DURATION_MS = 1500L;

    private HudNotifier() {
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register(HudNotifier::render);
    }

    public static void showExecuted() {
        shownAt = System.currentTimeMillis();
    }

    private static void render(DrawContext context, float tickDelta) {
        if (shownAt < 0L) {
            return;
        }

        long elapsed = System.currentTimeMillis() - shownAt;
        if (elapsed > DURATION_MS) {
            shownAt = -1L;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getWindow() == null) {
            return;
        }

        float progress = elapsed / (float) DURATION_MS;
        int alpha = (int) ((1.0F - progress) * 255.0F);
        int color = (alpha << 24) | 0x55FF55;

        int centerX = client.getWindow().getScaledWidth() / 2;
        int y = client.getWindow().getScaledHeight() - 45 - (int) (progress * 8);

        context.drawCenteredTextWithShadow(client.textRenderer, "Исполнено", centerX, y, color);
    }
}
