package petrolpark.mc.destroy.core.chemistry.hazard.mobeffect;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import petrolpark.mc.destroy.core.mobeffect.DestroyMobEffect;

/**
 * Tears-triggering effect applied by {@code ChemistryHazardHelper.damage} when the entity is
 * exposed to a {@code LACRIMATOR}-tagged Molecule without eye protection.
*/
public class CryingMobEffect extends DestroyMobEffect {

    public CryingMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
 * Broadcast the crying-started signal to clients so remote entities render tears immediately.
 * Called from {@code ChemistryHazardHelper.damage} when the effect is first applied.
*/
    public static void broadcastCryingStarted(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            CatnipServices.NETWORK.sendToClientsTrackingEntity(entity, new CryingS2CPacket(entity, true));
        }
    }

    /**
 * Broadcast the crying-stopped signal. Called when the entity's crying effect ends.
 * (Currently unused — 1.21 attribute-modifier API shift means we don't have a natural "effect
 * expired" hook; the client-side effect will simply time out on its own.)
*/
    public static void broadcastCryingStopped(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            CatnipServices.NETWORK.sendToClientsTrackingEntity(entity, new CryingS2CPacket(entity, false));
        }
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        // Spawn TEAR particles SERVER-side via {@link ServerLevel#sendParticles} broadcast
        // to all clients tracking this entity. Replaces the client-side
        // {@code level.addParticle(...)} path that depended on:
        // (a) MobEffectInstance reaching client via vanilla sync,
        // (b) Client tick firing applyEffectTick on the client copy of the entity,
        // (c) Per-client RNG.
        // Real-world testing showed villager tears were silently absent on dedicated-server play
        // even after wired broadcastCryingStarted — likely because the natural client-side
        // tick chain has subtle gaps (MobEffectInstance sync timing for newly-applied non-visible
        // effects on remote entities). Server-side sendParticles is unconditional: whoever's
        // tracking the entity sees the particle, period.
        // First-person eye-offset adjustment is sacrificed — server can't know what
        // camera each remote client uses. For self-crying this means the player sees tears at full
        // eye height instead of slightly below in first-person. Acceptable tradeoff.
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            RandomSource rand = livingEntity.getRandom();
            if (rand.nextFloat() > 0.8f) {
                Vec3 pos = livingEntity.getEyePosition();
                Vec3 motion = livingEntity.getDeltaMovement();
                double angleRad = Mth.PI * (livingEntity.getYHeadRot() + 90 - 15 + rand.nextFloat() * 30) / 180;
                double vx = motion.x + Mth.cos((float) angleRad) * 0.15d;
                double vy = motion.y;
                double vz = motion.z + Mth.sin((float) angleRad) * 0.15d;
                // count=0 → ServerLevel.sendParticles velocity-vector mode (the (xDist, yDist,
                // zDist) triple becomes the raw velocity, `speed` multiplier=1 keeps it as-is).
                serverLevel.sendParticles(new TearParticle.Data(), pos.x, pos.y, pos.z, 0, vx, vy, vz, 1.0d);
            }
        }
        return super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
