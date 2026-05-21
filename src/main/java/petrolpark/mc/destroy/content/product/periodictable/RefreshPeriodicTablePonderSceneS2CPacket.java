package petrolpark.mc.destroy.content.product.periodictable;

import io.netty.buffer.ByteBuf;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.client.DestroyPonderScenes;

/** Tells clients to
 * refresh their Ponder scene registry so newly-registered element blocks get the {@code
 * periodicTable} story-board auto-attached.
*/
public final class RefreshPeriodicTablePonderSceneS2CPacket implements ClientboundPacketPayload {

    public static final RefreshPeriodicTablePonderSceneS2CPacket INSTANCE = new RefreshPeriodicTablePonderSceneS2CPacket();
    public static final StreamCodec<ByteBuf, RefreshPeriodicTablePonderSceneS2CPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private RefreshPeriodicTablePonderSceneS2CPacket() {}

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REFRESH_PERIODIC_TABLE_PONDER_SCENE;
    }

    @Override
    public void handle(LocalPlayer player) {
        DestroyPonderScenes.refreshPeriodicTableBlockScenes();
    }
}
