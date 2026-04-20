package com.flopasss.proximitydeathsound.command;

import com.flopasss.proximitydeathsound.ProximityDeathSound;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class ProximityDeathSoundCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("proximitydeathsound").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("sound")
                        .then(argument("sound", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    // Suggest all sound event registry names
                                    for (SoundEvent soundEvent : BuiltInRegistries.SOUND_EVENT) {
                                        var key = BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
                                        if (key != null) {
                                            builder.suggest(key.toString());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String sound = StringArgumentType.getString(context, "sound");

                                    ProximityDeathSound.CONFIG.sound = sound;
                                    ProximityDeathSound.CONFIG.save();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Sound effect set to: " + sound), true);
                                    return 1;
                                })))
                .then(literal("source")
                        .then(argument("source", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    // Suggest all sound source categories
                                    for (SoundSource source : SoundSource
                                            .values()) {
                                        builder.suggest(source.getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String source = StringArgumentType.getString(context, "source");

                                    ProximityDeathSound.CONFIG.source = source;
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
                                .suggests((context, builder) -> {
                                    for (int i = 0; i <= 2; i++) {
                                        builder.suggest(i);
                                    }
                                    return builder.buildFuture();
                                })
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
