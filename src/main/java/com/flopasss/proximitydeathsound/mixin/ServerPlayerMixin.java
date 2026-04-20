package com.flopasss.proximitydeathsound.mixin;

import com.flopasss.proximitydeathsound.ProximityDeathSound;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
    }
}
