package com.flopasss.proximitydeathsound.mixin;

import com.flopasss.proximitydeathsound.ProximityDeathSound;
import com.flopasss.proximitydeathsound.config.ProximityDeathSoundConfig;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(at = @At("HEAD"), method = "die")
    private void onDie(DamageSource damageSource, CallbackInfo callbackInfo) {
        ProximityDeathSoundConfig config = ProximityDeathSound.CONFIG;
        Holder.Reference<SoundEvent> soundEvent = config.getResolvedSoundEvent();
        SoundSource soundSource = config.getResolvedSoundSource();

        // Silently skip if the config is invalid — the warning was emitted
        // once at load/command time, no need to spam on every death.
        if (soundEvent == null || soundSource == null)
            return;

        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel world = (ServerLevel) player.level();

        // Play the sound to all players in the dimension
        world.playSound(null,
                player.getX(), player.getY(), player.getZ(),
                soundEvent, soundSource,
                (float) config.volume, (float) config.pitch);
    }
}
