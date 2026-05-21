package petrolpark.mc.destroy.core.chemistry.vat.observation;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import petrolpark.mc.destroy.DestroyPackets;

/**
 * Client → server packet: set the lower + upper redstone-quantity thresholds on a Vat-side BE's
 * {@link RedstoneQuantityMonitorBehaviour}. Sent by {@code RedstoneMonitorVatSideScreen} slider
 * drag + release.
*/
public record RedstoneQuantityMonitorThresholdChangeC2SPacket(float lower, float upper, BlockPos pos)
    implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, RedstoneQuantityMonitorThresholdChangeC2SPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.FLOAT, RedstoneQuantityMonitorThresholdChangeC2SPacket::lower,
            ByteBufCodecs.FLOAT, RedstoneQuantityMonitorThresholdChangeC2SPacket::upper,
            BlockPos.STREAM_CODEC, RedstoneQuantityMonitorThresholdChangeC2SPacket::pos,
            RedstoneQuantityMonitorThresholdChangeC2SPacket::new);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REDSTONE_QUANTITY_MONITOR_THRESHOLD_CHANGE;
    }

    @Override
    public void handle(ServerPlayer player) {
        RedstoneQuantityMonitorBehaviour behaviour = BlockEntityBehaviour.get(player.level(), pos, RedstoneQuantityMonitorBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.lowerThreshold = lower;
            behaviour.upperThreshold = upper;
            behaviour.notifyUpdate();
        }
    }
}
