package com.flopasss.proximitydeathsound.command;

import com.flopasss.proximitydeathsound.ProximityDeathSound;
import com.flopasss.proximitydeathsound.config.ProximityDeathSoundConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ProximityDeathSoundCommand {
    // Precomputed suggestion arrays so we don't rebuild them on every keystroke.
    private static final String[] SOUND_SOURCE_NAMES = buildSoundSourceNames();
    private static final String[] PITCH_VALUES = { "0", "1", "2" };

    private static String[] buildSoundSourceNames() {
        SoundSource[] values = SoundSource.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].getName();
        }
        return names;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("proximitydeathsound").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("sound")
                        .then(argument("sound", StringArgumentType.greedyString())
                                // SharedSuggestionProvider.suggestResource does prefix
                                // filtering on the registry's key set directly, avoiding
                                // the per-keystroke full-registry iteration + getKey()
                                // lookup the previous implementation did.
                                .suggests((context, builder) -> SharedSuggestionProvider
                                        .suggestResource(BuiltInRegistries.SOUND_EVENT.keySet(), builder))
                                .executes(context -> {
                                    String sound = StringArgumentType.getString(context, "sound");

                                    ProximityDeathSound.CONFIG.sound = sound;
                                    ProximityDeathSound.CONFIG.resolve();
                                    ProximityDeathSound.CONFIG.save();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Sound effect set to: " + sound), true);
                                    return 1;
                                })))
                .then(literal("source")
                        .then(argument("source", StringArgumentType.string())
                                .suggests((context, builder) -> SharedSuggestionProvider
                                        .suggest(SOUND_SOURCE_NAMES, builder))
                                .executes(context -> {
                                    String source = StringArgumentType.getString(context, "source");

                                    if (!ProximityDeathSoundConfig.isValidSoundSource(source)) {
                                        context.getSource().sendFailure(
                                                Component.literal("Invalid sound source: " + source));
                                        return 0;
                                    }

                                    ProximityDeathSound.CONFIG.source = source;
                                    ProximityDeathSound.CONFIG.resolve();
                                    ProximityDeathSound.CONFIG.save();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Sound category set to: " + source), true);
                                    return 1;
                                })))
                .then(literal("volume")
                        .then(argument("volume", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    int volume = IntegerArgumentType.getInteger(context, "volume");

                                    ProximityDeathSound.CONFIG.volume = volume;
                                    ProximityDeathSound.CONFIG.save();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Chunk range set to: " + volume), true);
                                    return 1;
                                })))
                .then(literal("pitch")
                        .then(argument("pitch", IntegerArgumentType.integer(0, 2))
                                .suggests((context, builder) -> SharedSuggestionProvider
                                        .suggest(PITCH_VALUES, builder))
                                .executes(context -> {
                                    int pitch = IntegerArgumentType.getInteger(context, "pitch");

                                    ProximityDeathSound.CONFIG.pitch = pitch;
                                    ProximityDeathSound.CONFIG.save();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Pitch set to: " + pitch), true);
                                    return 1;
                                })))
                .then(literal("status")
                        .executes(context -> {
                            String sound = ProximityDeathSound.CONFIG.sound;
                            String source = ProximityDeathSound.CONFIG.source;
                            int volume = ProximityDeathSound.CONFIG.volume;
                            int pitch = ProximityDeathSound.CONFIG.pitch;

                            context.getSource().sendSuccess(
                                    () -> Component.literal("Flopasss Proximity Death Sound Configuration:")
                                            .append(Component.literal("\nSound: " + sound))
                                            .append(Component.literal("\nSource: " + source))
                                            .append(Component.literal("\nVolume: " + volume))
                                            .append(Component.literal("\nPitch: " + pitch)),
                                    true);
                            return 1;
                        })));
    }
}
