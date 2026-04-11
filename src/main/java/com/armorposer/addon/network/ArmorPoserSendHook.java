package com.armorposer.addon.network;

import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

/**
 * Хук для Mixin-слоя: получает факт отправки синхронизационного пакета Armor Poser.
 */
public final class ArmorPoserSendHook {
    private ArmorPoserSendHook() {
    }

    public static void onBeforeSend(UUID standUuid, NbtCompound tag) {
        // Точка расширения: здесь можно добавить логи, троттлинг или аудит отправки.
    }
}
