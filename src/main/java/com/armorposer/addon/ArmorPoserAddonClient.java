package com.armorposer.addon;

import com.armorposer.addon.config.AddonConfig;
import com.armorposer.addon.gui.AddonScreen;
import com.armorposer.addon.gui.HudNotifier;
import com.armorposer.addon.network.ArmorStandAutomationService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ArmorPoserAddonClient implements ClientModInitializer {
    private static KeyBinding captureTargetKey;
    private static KeyBinding openScreenKey;

    @Override
    public void onInitializeClient() {
        AddonConfig.load();
        HudNotifier.initialize();
        captureTargetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.armorposer_addon.capture_target",
                InputUtil.Type.KEYSYM,
                AddonConfig.get().captureTargetKeyCode,
                "category.armorposer_addon.main"
        ));
        openScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.armorposer_addon.open_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.armorposer_addon.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        while (captureTargetKey.wasPressed()) {
            ArmorStandAutomationService.captureTarget(client);
        }
        while (openScreenKey.wasPressed()) {
            client.setScreen(new AddonScreen());
        }
    }
}
