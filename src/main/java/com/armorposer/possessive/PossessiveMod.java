package com.armorposer.possessive;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PossessiveMod implements ModInitializer {
    public static final String MOD_ID = "possessive";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Possessive loaded");
    }
}
