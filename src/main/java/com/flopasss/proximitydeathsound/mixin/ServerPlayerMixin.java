package com.flopasss.proximitydeathsound.mixin;

import com.flopasss.proximitydeathsound.ProximityDeathSound;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.resources.Identifier;
import java.util.Optional;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(at = @At("HEAD"), method = "die")
    private void onDie(DamageSource damageSource, CallbackInfo callbackInfo) {
        // Get the player
        ServerPlayer player = (ServerPlayer) (Object) this;
        // Get the player's dimension
        ServerLevel world = (ServerLevel) player.level();

        // Get the config values
        String sound = ProximityDeathSound.CONFIG.sound;
        String source = ProximityDeathSound.CONFIG.source;
        float volume = (float) ProximityDeathSound.CONFIG.volume;
        float pitch = (float) ProximityDeathSound.CONFIG.pitch;

        // Validate the sound
        Identifier soundId = Identifier.tryParse(sound);
        var soundHolder = soundId == null
                ? Optional.<Holder.Reference<SoundEvent>>empty()
                : BuiltInRegistries.SOUND_EVENT.get(soundId);
        if (soundHolder.isEmpty()) {
            ProximityDeathSound.LOGGER.warn("Invalid sound identifier: " + sound);
            return;
        }

        // Validate the sound source
        SoundSource soundSource;
        try {
            soundSource = SoundSource.valueOf(source.toUpperCase());
        } catch (Exception e) {
            ProximityDeathSound.LOGGER.warn("Invalid sound source: " + source);
            return;
        }

        // Play the sound to all players in the dimension
        world.playSound(null, player.getX(), player.getY(), player.getZ(), soundHolder.get(), soundSource, volume,
                pitch);
    }
}