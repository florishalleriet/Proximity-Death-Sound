package com.flopasss.proximitydeathsound.mixin;
import com.flopasss.proximitydeathsound.ProximityDeathSound;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
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
        // Get the player
        ServerPlayer player = (ServerPlayer) (Object) this;

        // Get the player's coordinates and world
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        ServerLevel world = (ServerLevel) player.level();

        // Get the config values
        String soundEffect = ProximityDeathSound.CONFIG.soundEffect;
        String soundCategory = ProximityDeathSound.CONFIG.soundCategory;
        float chunkRange = (float) ProximityDeathSound.CONFIG.chunkRange;
        
        double rangeInBlocks = chunkRange * 16.0;
        
        // Find sound event by registry name
        var soundEventOptional = BuiltInRegistries.SOUND_EVENT.stream()
            .filter(s -> BuiltInRegistries.SOUND_EVENT.getKey(s).toString().equals(soundEffect))
            .findFirst();
        
        if (soundEventOptional.isEmpty()) {
            ProximityDeathSound.LOGGER.warn("Invalid sound effect in config: {}", soundEffect);
            return;
        }
        
        SoundEvent soundEvent = soundEventOptional.get();
        
        world.players().stream()
                .filter(p -> p.distanceToSqr(x, y, z) <= rangeInBlocks * rangeInBlocks)
                .forEach(p -> p.connection.send(new ClientboundSoundPacket(
                    Holder.direct(soundEvent),
                    SoundSource.valueOf(soundCategory.toUpperCase()),
                    x, y, z,
                    chunkRange,
                    1.0f,
                    0L
                )));
    }
}