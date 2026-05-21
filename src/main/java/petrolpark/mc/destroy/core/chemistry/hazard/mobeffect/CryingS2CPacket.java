package petrolpark.mc.destroy.core.chemistry.hazard.mobeffect;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.DestroyPackets;

/**
 * Tells the client that a remote entity started/stopped crying so its {@link TearParticle}
 * particles render immediately without requiring a full effect-sync round-trip.
*/
public record CryingS2CPacket(boolean isCrying, int entityId) implements ClientboundPacketPayload {

    public CryingS2CPacket(LivingEntity entity, boolean isCrying) {
        this(isCrying, entity.getId());
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CryingS2CPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL,    CryingS2CPacket::isCrying,
            ByteBufCodecs.VAR_INT, CryingS2CPacket::entityId,
            CryingS2CPacket::new
        );

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CRYING;
    }

    @Override
    public void handle(LocalPlayer ignored) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof LivingEntity livingEntity)) return;

        if (isCrying && !livingEntity.hasEffect(DestroyMobEffects.CRYING.getDelegate())) {
            livingEntity.addEffect(new MobEffectInstance(
                DestroyMobEffects.CRYING.getDelegate(),
                Integer.MAX_VALUE, 0, true, false, false));
        } else if (!isCrying) {
            livingEntity.removeEffect(DestroyMobEffects.CRYING.getDelegate());
        }
    }
}
