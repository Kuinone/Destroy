package petrolpark.mc.destroy.content.redstone.programmer;

import io.netty.buffer.ByteBuf;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;

import petrolpark.mc.destroy.DestroyPackets;

/**
 * Server → client acknowledgement of {@link RedstoneProgramSyncC2SPacket} — empty payload, just
 * signals the client to {@code refreshSlots()} on its open Menu (slot count may have changed due
 * to channel add/remove).
*/
public final class RedstoneProgramSyncReplyS2CPacket implements ClientboundPacketPayload {

    public static final RedstoneProgramSyncReplyS2CPacket INSTANCE = new RedstoneProgramSyncReplyS2CPacket();
    public static final StreamCodec<ByteBuf, RedstoneProgramSyncReplyS2CPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REDSTONE_PROGRAM_SYNC_REPLY;
    }

    @Override
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.containerMenu instanceof RedstoneProgrammerMenu menu) {
            menu.refreshSlots();
        }
    }
}
