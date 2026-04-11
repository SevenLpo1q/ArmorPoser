package com.armorposer.addon.network;

import net.minecraft.nbt.NbtCompound;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * Мост к API Armor Poser через reflection.
 *
 * Это позволяет аддону компилироваться без жесткой compile-time зависимости,
 * но в рантайме использовать методы мода Armor Poser 8.0.1 для синхронизации CompoundTag.
 */
public final class ArmorPoserApiBridge {
    private static final String BRIDGE_CLASS = "io.github.foundationgames.armorposer.client.networking.ArmorPoserClientNetworking";

    private ArmorPoserApiBridge() {
    }

    public static boolean sendStandState(UUID standUuid, NbtCompound syncTag) {
        return invokeBridge("sendSyncTag", standUuid, syncTag).isPresent();
    }

    public static boolean sendCorrectivePose(UUID standUuid, NbtCompound syncTag) {
        return invokeBridge("sendPoseCorrection", standUuid, syncTag).isPresent();
    }

    private static Optional<Object> invokeBridge(String methodName, UUID standUuid, NbtCompound syncTag) {
        try {
            Class<?> clazz = Class.forName(BRIDGE_CLASS);
            Method method = clazz.getMethod(methodName, UUID.class, NbtCompound.class);
            return Optional.ofNullable(method.invoke(null, standUuid, syncTag));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
