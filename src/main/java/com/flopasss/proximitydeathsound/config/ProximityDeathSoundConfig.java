package com.flopasss.proximitydeathsound.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.flopasss.proximitydeathsound.ProximityDeathSound;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ProximityDeathSoundConfig {
    public String sound = "minecraft:entity.wither.spawn"; // Default sound
    public String source = "ambient"; // Default source
    public int volume = 16; // Default volume
    public int pitch = 1; // Default pitch

    // Cached, resolved values. Populated by resolve() and consumed by the
    // death-event hot path so it never has to parse strings, hit the registry,
    // or do exception-based enum validation per death.
    // Marked transient so Gson neither serializes nor overwrites them on load.
    private transient Holder.Reference<SoundEvent> resolvedSoundEvent;
    private transient SoundSource resolvedSoundSource;

    // Name -> SoundSource lookup, built once. SoundSource.getName() values are
    // the canonical lowercase names (e.g. "ambient", "record", "block").
    private static final Map<String, SoundSource> SOUND_SOURCE_BY_NAME = buildSoundSourceLookup();

    private static Map<String, SoundSource> buildSoundSourceLookup() {
        SoundSource[] values = SoundSource.values();
        Map<String, SoundSource> map = new HashMap<>(values.length);
        for (SoundSource s : values) {
            map.put(s.getName(), s);
        }
        return map;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create(); // Gson instance for JSON
                                                                                     // serialization/deserialization
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(ProximityDeathSound.MOD_ID + ".json"); // Path to the configuration file

    public static ProximityDeathSoundConfig load() {
        ProximityDeathSoundConfig config = null;

        if (Files.exists(CONFIG_PATH)) {
            try {
                // Read the JSON file and deserialize into a config instance
                String json = Files.readString(CONFIG_PATH);
                config = GSON.fromJson(json, ProximityDeathSoundConfig.class);
            } catch (Exception e) {
                ProximityDeathSound.LOGGER.error("Failed to load config, using default values", e);
            }
        }

        if (config == null) {
            // No file (or load failed): use defaults and persist them
            config = new ProximityDeathSoundConfig();
            config.save();
        }

        // Pre-resolve cached values so the death hot path is allocation-free
        config.resolve();
        return config;
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

    /**
     * Re-parses the string config fields into their cached, fast-access forms.
     * Call this once at load and after any command mutation so the death
     * event handler can avoid {@link Identifier#tryParse}, a registry lookup,
     * and exception-based enum validation on every player death.
     */
    public void resolve() {
        // Sound event
        Identifier soundId = Identifier.tryParse(sound);
        Optional<Holder.Reference<SoundEvent>> holder = soundId == null
                ? Optional.empty()
                : BuiltInRegistries.SOUND_EVENT.get(soundId);
        if (holder.isPresent()) {
            resolvedSoundEvent = holder.get();
        } else {
            resolvedSoundEvent = null;
            ProximityDeathSound.LOGGER.warn("Invalid sound identifier: {}", sound);
        }

        // Sound source. Match against SoundSource.getName() (case-insensitive)
        // rather than SoundSource.valueOf(...toUpperCase()), which was both
        // allocating/exception-throwing and buggy for sources whose enum name
        // differs from their registered name (e.g. "record" vs RECORDS).
        SoundSource resolved = SOUND_SOURCE_BY_NAME.get(source.toLowerCase(Locale.ROOT));
        if (resolved != null) {
            resolvedSoundSource = resolved;
        } else {
            resolvedSoundSource = null;
            ProximityDeathSound.LOGGER.warn("Invalid sound source: {}", source);
        }
    }

    public Holder.Reference<SoundEvent> getResolvedSoundEvent() {
        return resolvedSoundEvent;
    }

    public SoundSource getResolvedSoundSource() {
        return resolvedSoundSource;
    }

    public static boolean isValidSoundSource(String name) {
        return name != null && SOUND_SOURCE_BY_NAME.containsKey(name.toLowerCase(Locale.ROOT));
    }
}
