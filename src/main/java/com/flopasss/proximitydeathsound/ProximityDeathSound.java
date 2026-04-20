package com.flopasss.proximitydeathsound;

import com.flopasss.proximitydeathsound.config.ProximityDeathSoundConfig;
import com.flopasss.proximitydeathsound.command.ProximityDeathSoundCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ProximityDeathSound implements ModInitializer {
	public static final String MOD_ID = "proximity-death-sound";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ProximityDeathSoundConfig CONFIG;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// Load the config on startup
		CONFIG = ProximityDeathSoundConfig.load();

		// Register the command
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ProximityDeathSoundCommand.register(dispatcher);
		});

		LOGGER.info("Flopasss Proximity Death Sound initialized");
	}
}