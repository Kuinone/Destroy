package petrolpark.mc.destroy.content.confetti;

import org.joml.Vector3f;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import petrolpark.mc.destroy.DestroyPackets;

/**
 * Server-to-client packet: spawn a burst of {@link ConfettoParticle} at a position with a velocity
 * cone seeded from the dispenser direction.
*/
public record ConfettiBurstS2CPacket(ItemStack confetti, Vector3f pos, Vector3f velocity)
    implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfettiBurstS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC, ConfettiBurstS2CPacket::confetti,
        ByteBufCodecs.VECTOR3F, ConfettiBurstS2CPacket::pos,
        ByteBufCodecs.VECTOR3F, ConfettiBurstS2CPacket::velocity,
        ConfettiBurstS2CPacket::new);

    public ConfettiBurstS2CPacket(ItemStack confetti, Vec3 pos, Vec3 velocity) {
        this(confetti,
            new Vector3f((float) pos.x, (float) pos.y, (float) pos.z),
            new Vector3f((float) velocity.x, (float) velocity.y, (float) velocity.z));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CONFETTI_BURST;
    }

    @Override
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        if (!(confetti.getItem() instanceof ConfettiItem item)) return;
        RandomSource rand = level.random;
        for (int i = 0; i < 256; i++) {
            Vec3 v = new Vec3(velocity).add(new Vec3(
                -0.05d + rand.nextFloat() * 0.1d,
                -0.05d + rand.nextFloat() * 0.1d,
                -0.05d + rand.nextFloat() * 0.1d));
            level.addParticle(item.particleFactory.get(), pos.x(), pos.y(), pos.z(), v.x(), v.y(), v.z());
        }
    }
}
