package ru.swill.hitbox.client;

import ru.swill.hitbox.HitboxState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HitboxClient implements ClientModInitializer {
    private static KeyBinding shrinkKey;
    private static KeyBinding growKey;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        shrinkKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hitboxmod.shrink",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                "category.hitboxmod"
        ));

        growKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hitboxmod.grow",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                "category.hitboxmod"
        ));

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hitboxmod.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DELETE,
                "category.hitboxmod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (toggleKey.wasPressed()) {
                HitboxState.enabled = !HitboxState.enabled;
                if (!HitboxState.enabled) {
                    HitboxState.resetToDefault();
                }
                client.player.calculateDimensions();
            }

            if (!HitboxState.enabled) return;

            boolean changed = false;
            if (shrinkKey.isPressed()) {
                HitboxState.decrease();
                changed = true;
            }
            if (growKey.isPressed()) {
                HitboxState.increase();
                changed = true;
            }

            if (changed) {
                client.player.calculateDimensions();
            }
        });
    }
}
