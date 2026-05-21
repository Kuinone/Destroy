package petrolpark.mc.destroy.content.processing.trypolithography.keypunch;

import io.netty.buffer.ByteBuf;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import petrolpark.mc.destroy.DestroyPackets;

/**
 * Client → server: set the Keypunch's display name. Sent from {@link KeypunchScreen} when the
 * player commits a name change (Enter on EditBox / Screen close).
*/
public record NameKeypunchC2SPacket(BlockPos pos, String name) implements ServerboundPacketPayload {

    public static final StreamCodec<ByteBuf, NameKeypunchC2SPacket> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,       NameKeypunchC2SPacket::pos,
            ByteBufCodecs.STRING_UTF8,   NameKeypunchC2SPacket::name,
            NameKeypunchC2SPacket::new);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.NAME_KEYPUNCH;
    }

    @Override
    public void handle(ServerPlayer player) {
        Level level = player.level();
        if (level == null) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KeypunchBlockEntity keypunch) {
            keypunch.name = name;
            keypunch.notifyUpdate();
        }
    }
}
