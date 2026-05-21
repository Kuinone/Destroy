package petrolpark.mc.destroy.content.redstone.programmer;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgrammerMenu.DummyRedstoneProgram;

/**
 * Client → server: sync the full {@link RedstoneProgram} state from a Menu-open Player to the
 * server-side authoritative copy. Triggered when a player changes a frequency slot or modifies
 * a channel sequence in the Menu UI.
*/
public final class RedstoneProgramSyncC2SPacket implements ServerboundPacketPayload {

    public final RedstoneProgram program;

    public RedstoneProgramSyncC2SPacket(RedstoneProgram program) {
        this.program = program;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, RedstoneProgramSyncC2SPacket> STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> packet.program.write(buf),
            buf -> {
                DummyRedstoneProgram p = new DummyRedstoneProgram();
                p.read(buf);
                return new RedstoneProgramSyncC2SPacket(p);
            });

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REDSTONE_PROGRAM_SYNC;
    }

    @Override
    public void handle(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof RedstoneProgrammerMenu programMenu) {
            RedstoneProgram contentHolder = programMenu.contentHolder;
            contentHolder.unload();
            contentHolder.copyFrom(this.program);
            contentHolder.load();
            contentHolder.whenChanged();
            // must use singleton INSTANCE not a fresh `new`. The packet's STREAM_CODEC
            // is `StreamCodec.unit(INSTANCE)` which fail-asserts encoded value is reference-equal
            // to INSTANCE. `new ...()` makes a different instance → encoder throws "Can't encode
            // 'X@hashA', expected 'X@hashB'" → server kicks client on encode fail.
            CatnipServices.NETWORK.sendToClient(player, RedstoneProgramSyncReplyS2CPacket.INSTANCE);
            if (Math.min(contentHolder.getChannels().size() + 1,
                    DestroyAllConfigs.SERVER.blocks.redstoneProgrammerMaxChannels.get()) * 2 > programMenu.slots.size()) {
                programMenu.refreshSlots();
            }
        }
    }
}
