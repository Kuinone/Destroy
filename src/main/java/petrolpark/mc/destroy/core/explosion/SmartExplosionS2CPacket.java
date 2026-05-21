package petrolpark.mc.destroy.core.explosion;

import java.util.List;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import petrolpark.mc.destroy.DestroyPackets;

/**
 * Server-to-client sync for a {@link SmartExplosion}: replay {@code finalizeExplosion(true)} on
 * the client so particles/sound/block-remove animations fire, and apply the recipient's knockback
 * vector (which the server already decided server-side in {@code explodeEntity}).*/
public record SmartExplosionS2CPacket(Vec3 position, float radius, float irregularity,
                                       List<BlockPos> toBlow, Vec3 recipientKnockback)
    implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SmartExplosionS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list()).map(
            list -> new Vec3(list.get(0), list.get(1), list.get(2)),
            v -> List.of(v.x, v.y, v.z)),
        SmartExplosionS2CPacket::position,
        ByteBufCodecs.FLOAT, SmartExplosionS2CPacket::radius,
        ByteBufCodecs.FLOAT, SmartExplosionS2CPacket::irregularity,
        BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), SmartExplosionS2CPacket::toBlow,
        ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list()).map(
            list -> new Vec3(list.get(0), list.get(1), list.get(2)),
            v -> List.of(v.x, v.y, v.z)),
        SmartExplosionS2CPacket::recipientKnockback,
        SmartExplosionS2CPacket::new);

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SMART_EXPLOSION;
    }

    /** The actual client logic
 * (Minecraft.getInstance / ClientLevel / SmartExplosion construction) lives in
 * {@link ClientHandler}, which is loaded only on physical client. On
 * {@link Dist#DEDICATED_SERVER} this method's body is a no-op (the FMLEnvironment.dist guard
 * short-circuits before ClientHandler is referenced) and {@code ClientHandler.class} is
 * never resolved, so the dedicated-server class loader never attempts to bring in
 * {@code Minecraft / ClientLevel}.
*/
    @Override
    public void handle(LocalPlayer player) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.handle(this, player);
        }
    }

    /** Resolved + class-loaded only on {@link Dist#CLIENT}.
 * References {@link net.minecraft.client.Minecraft} / {@link net.minecraft.client.multiplayer.ClientLevel} /
 * {@link SmartExplosion} freely; on dedicated server the JVM never loads this nested class
 * because the {@link #handle} guard branches around it.
*/
    public static final class ClientHandler {
        private ClientHandler() {}

        public static void handle(SmartExplosionS2CPacket packet, LocalPlayer player) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            net.minecraft.client.multiplayer.ClientLevel level = mc.level;
            if (level == null) return;
            SmartExplosion explosion = new SmartExplosion(level, null, null, null,
                packet.position, packet.radius, packet.irregularity);
            explosion.getToBlow().addAll(packet.toBlow);
            explosion.finalizeExplosion(true);
            if (mc.player != null) mc.player.setDeltaMovement(packet.recipientKnockback);
        }
    }
}
