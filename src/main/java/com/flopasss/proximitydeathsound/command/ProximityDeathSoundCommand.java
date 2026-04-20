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
                .then(literal("soundeffect")
                        .then(argument("soundeffect", StringArgumentType.greedyString())
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
                                    String soundEffect = StringArgumentType.getString(context, "soundeffect");

                                    ProximityDeathSound.CONFIG.soundEffect = soundEffect;
                                    ProximityDeathSound.CONFIG.save();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Sound effect set to: " + soundEffect), true);
                                    return 1;
                                })))
                .then(literal("soundcategory")
                        .then(argument("soundcategory", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    // Suggest all sound source categories
                                    for (SoundSource source : SoundSource
                                            .values()) {
                                        builder.suggest(source.getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String soundCategory = StringArgumentType.getString(context, "soundcategory");

                                    ProximityDeathSound.CONFIG.soundCategory = soundCategory;
                                    ProximityDeathSound.CONFIG.save();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Sound category set to: " + soundCategory), true);
                                    return 1;
                                })))
                .then(literal("chunkrange")
                        .then(argument("chunkrange", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    int chunkRange = IntegerArgumentType.getInteger(context, "chunkrange");

                                    ProximityDeathSound.CONFIG.chunkRange = chunkRange;
                                    ProximityDeathSound.CONFIG.save();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Chunk range set to: " + chunkRange), true);
                                    return 1;
                                }))));
    }
}
