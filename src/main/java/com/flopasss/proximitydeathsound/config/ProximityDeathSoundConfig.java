package com.flopasss.proximitydeathsound.config;

import java.nio.file.Files;
import java.nio.file.Path;

import com.flopasss.proximitydeathsound.ProximityDeathSound;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public class ProximityDeathSoundConfig {
    public String sound = "minecraft:entity.wither.spawn"; // Default sound
    public String source = "ambient"; // Default source
    public int volume = 16; // Default volume
    public int pitch = 1; // Default pitch

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create(); // Gson instance for JSON
                                                                                     // serialization/deserialization
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(ProximityDeathSound.MOD_ID + ".json"); // Path to the configuration file

    public static ProximityDeathSoundConfig load() {
        // Return default config if file doesn't exist
        if (!Files.exists(CONFIG_PATH)) {
            ProximityDeathSoundConfig defaultConfig = new ProximityDeathSoundConfig();
            defaultConfig.save(); // Save the default configuration to create the file
            return defaultConfig;
        }

        try {
            // Read the JSON file into a string
            String json = Files.readString(CONFIG_PATH);

            // Deserialize the JSON string into a ProximityDeathSoundConfig variable
            ProximityDeathSoundConfig config = GSON.fromJson(json, ProximityDeathSoundConfig.class);

            // If the config is null, return default config
            if (config == null)
                return new ProximityDeathSoundConfig();

            // Return the loaded config
            return config;

        } catch (Exception e) {
            // Log the error
            ProximityDeathSound.LOGGER.error("Failed to load config, using default values", e);

            // Return default config if there's an exception
            return new ProximityDeathSoundConfig();
        }
    }

    public void save() {
        try {
            // Serialize the current config instance to a JSON string
            String json = GSON.toJson(this);

            // Write the JSON string to the configuration file
            Files.writeString(CONFIG_PATH, json);
        } catch (Exception e) {
            // Log any errors that occur during saving
            ProximityDeathSound.LOGGER.error("Failed to save config", e);
        }
    }
}
