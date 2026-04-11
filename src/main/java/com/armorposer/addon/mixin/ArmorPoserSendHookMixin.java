package com.armorposer.addon.mixin;

import com.armorposer.addon.network.ArmorPoserSendHook;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Интеграция в слой отправки пакетов Armor Poser.
 *
 * targets + remap=false позволяют мягко зацепиться за внешний класс,
 * не ломая компиляцию при отсутствии исходников Armor Poser в compile classpath.
 */
@Mixin(targets = "io.github.foundationgames.armorposer.client.networking.ArmorPoserClientNetworking", remap = false)
public abstract class ArmorPoserSendHookMixin {
    @Inject(method = "sendSyncTag", at = @At("HEAD"), remap = false)
    private static void armorposerAddon$onSyncTag(UUID standUuid, NbtCompound tag, CallbackInfo ci) {
        ArmorPoserSendHook.onBeforeSend(standUuid, tag);
    }
}
