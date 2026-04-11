package com.armorposer.addon.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

/**
 * Центральная логика автоматизации цикла взаимодействия со стойкой.
 */
public final class ArmorStandAutomationService {
    private static UUID capturedStandUuid;

    private ArmorStandAutomationService() {
    }

    /**
     * Сохраняет UUID стойки, на которую смотрит игрок.
     */
    public static void captureTarget(MinecraftClient client) {
        if (client == null || client.crosshairTarget == null || client.player == null || client.world == null) {
            return;
        }

        Entity target = client.targetedEntity;
        if (target instanceof ArmorStandEntity stand) {
            capturedStandUuid = stand.getUuid();
        }
    }

    /**
     * Выполняет рабочий цикл:
     * 1) Смещает стойку для взаимодействия,
     * 2) передает предмет и NBT в руку стойки,
     * 3) инициирует стадию "interacting",
     * 4) отправляет корректирующий пакет для возврата стойки.
     */
    public static boolean useSelectedItem(MinecraftClient client, String itemId, String nbtJson) {
        if (capturedStandUuid == null || client == null || client.player == null) {
            return false;
        }

        NbtCompound interactionTag = new NbtCompound();
        interactionTag.putString("phase", "prepare_interaction");
        interactionTag.putString("item_id", itemId);
        interactionTag.putString("item_nbt_json", nbtJson);
        interactionTag.putUuid("player_uuid", client.player.getUuid());

        // Шаг 1-3: первичный sync-пакет через API Armor Poser.
        boolean applied = ArmorPoserApiBridge.sendStandState(capturedStandUuid, interactionTag);
        if (!applied) {
            return false;
        }

        // Шаг 4: корректирующий пакет с возвратом стойки в исходную позицию.
        NbtCompound correctionTag = new NbtCompound();
        correctionTag.putString("phase", "restore_pose");
        correctionTag.putUuid("stand_uuid", capturedStandUuid);

        ArmorPoserApiBridge.sendCorrectivePose(capturedStandUuid, correctionTag);
        return true;
    }
}
