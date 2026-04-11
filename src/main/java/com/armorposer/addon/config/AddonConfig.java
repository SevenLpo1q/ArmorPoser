package com.armorposer.addon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Конфигурация аддона хранится в JSON, чтобы пользователь мог быстро менять бинды и лимиты
 * без перекомпиляции мода.
 */
public final class AddonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("armorposer-addon.json");

    private static AddonConfig instance;

    public int captureTargetKeyCode = 71; // G
    public int maxNbtJsonLength = 32000;

    private AddonConfig() {
    }

    public static AddonConfig get() {
        if (instance == null) {
            instance = new AddonConfig();
        }
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(reader, AddonConfig.class);
            } catch (IOException ignored) {
                instance = new AddonConfig();
            }
        } else {
            instance = new AddonConfig();
            save();
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(get(), writer);
        } catch (IOException ignored) {
            // В аварийной ситуации мод продолжит работу на текущих значениях в памяти.
        }
    }
}
