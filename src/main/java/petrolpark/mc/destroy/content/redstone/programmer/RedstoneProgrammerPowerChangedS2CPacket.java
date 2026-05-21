package petrolpark.mc.destroy.content.redstone.programmer;

import io.netty.buffer.ByteBuf;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgrammerMenu.DummyRedstoneProgram;

/**
 * Server → client: per-tick power-state sync for a player with an open Redstone Programmer Menu.
 * Server side watches its authoritative {@link RedstoneProgram#hasPower}; on state change,
 * broadcasts to the single player. Client caches the new value into its {@link DummyRedstoneProgram}
 * so the Screen can show the correct powered/unpowered indicator.
*/
public record RedstoneProgrammerPowerChangedS2CPacket(boolean powered) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, RedstoneProgrammerPowerChangedS2CPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, RedstoneProgrammerPowerChangedS2CPacket::powered,
            RedstoneProgrammerPowerChangedS2CPacket::new);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REDSTONE_PROGRAMMER_POWER_CHANGED;
    }

    @Override
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof RedstoneProgrammerScreen screen) {
            if (screen.getMenu().contentHolder instanceof DummyRedstoneProgram program) {
                program.powered = powered;
            }
        }
    }
}
