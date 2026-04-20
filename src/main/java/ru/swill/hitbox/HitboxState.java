package ru.swill.hitbox;

public final class HitboxState {
    private HitboxState() {}

    public static boolean enabled = true;
    public static float scale = 1.0f;

    public static final float MIN = 0.35f;
    public static final float MAX = 2.0f;
    public static final float STEP_PER_TICK = 0.03f;

    public static void resetToDefault() {
        scale = 1.0f;
    }

    public static void increase() {
        scale = Math.min(MAX, scale + STEP_PER_TICK);
    }

    public static void decrease() {
        scale = Math.max(MIN, scale - STEP_PER_TICK);
    }
}
