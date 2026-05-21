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
 * Client → server: change the Keypunch's piston position (0-15 index into its 4×4 punch grid).
 * Sent from {@link KeypunchScreen} when the player clicks a position button. Server applies via
 * {@link KeypunchBlockEntity#setPistonPosition(int)} which also resets punch-tracking state +
 * notifies block update.
*/
public record ChangeKeypunchPositionC2SPacket(BlockPos pos, int pistonPosition) implements ServerboundPacketPayload {

    public static final StreamCodec<ByteBuf, ChangeKeypunchPositionC2SPacket> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, ChangeKeypunchPositionC2SPacket::pos,
            ByteBufCodecs.VAR_INT,  ChangeKeypunchPositionC2SPacket::pistonPosition,
            ChangeKeypunchPositionC2SPacket::new);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CHANGE_KEYPUNCH_POSITION;
    }

    @Override
    public void handle(ServerPlayer player) {
        Level level = player.level();
        if (level == null) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KeypunchBlockEntity keypunch) {
            keypunch.setPistonPosition(pistonPosition);
        }
    }
}
