package swill.way.appleskin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AppleSkinClient implements ClientModInitializer {
    private static KeyBinding increaseHitboxKey;
    private static KeyBinding decreaseHitboxKey;

    private static double extraHitbox = 0.0D;
    private static final double STEP = 0.04D;
    private static final double MIN = 0.0D;
    private static final double MAX = 2.0D;

    public static double getExtraHitbox() {
        return extraHitbox;
    }

    @Override
    public void onInitializeClient() {
        increaseHitboxKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.appleskin.increase_hitbox",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.appleskin.controls"
        ));

        decreaseHitboxKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.appleskin.decrease_hitbox",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.appleskin.controls"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (increaseHitboxKey.isPressed()) {
                extraHitbox = Math.min(MAX, extraHitbox + STEP);
            }

            if (decreaseHitboxKey.isPressed()) {
                extraHitbox = Math.max(MIN, extraHitbox - STEP);
            }
        });
    }
}
