package com.armorposer.possessive;

import com.armorposer.possessive.mixin.EntityNoClipAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerAbilities;
import org.lwjgl.glfw.GLFW;

public class PossessiveClient implements ClientModInitializer {
    private static KeyBinding toggleNoClipKey;
    private static boolean enabled = false;

    @Override
    public void onInitializeClient() {
        toggleNoClipKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.possessive.toggle_noclip",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.possessive.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        while (toggleNoClipKey.wasPressed()) {
            enabled = !enabled;
            PossessiveMod.LOGGER.info("No-clip {}", enabled ? "enabled" : "disabled");
        }

        PlayerAbilities abilities = client.player.getAbilities();

        if (enabled) {
            abilities.allowFlying = true;
            abilities.flying = true;
            abilities.setFlySpeed(0.15f);
            ((EntityNoClipAccessor) client.player).setNoClip(true);
            client.player.setOnGround(false);
            client.player.fallDistance = 0.0f;
        } else {
            ((EntityNoClipAccessor) client.player).setNoClip(false);
        }
    }
}
