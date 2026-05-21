package petrolpark.mc.destroy.content.processing.trypolithography.keypunch;

import java.util.List;

import io.netty.buffer.ByteBuf;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.util.NameLists;

/**
 * Server → client: prompt the placer's client to pick a random default name for a newly-placed
 * Keypunch. Triggered by {@link KeypunchBlockEntity#tick} on the server the first time the BE
 * ticks with {@code namedYet == false} and its placer is online. Client handler picks a random
 * name from the {@code data/destroy/name_lists/keypunch.json} datapack list and echoes back via
 * {@link NameKeypunchC2SPacket} so the server writes it into the BE.
*/
public record RequestKeypunchNameS2CPacket(BlockPos pos) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, RequestKeypunchNameS2CPacket> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, RequestKeypunchNameS2CPacket::pos,
            RequestKeypunchNameS2CPacket::new);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REQUEST_KEYPUNCH_NAME;
    }

    @Override
    public void handle(LocalPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        List<String> names = NameLists.getNames(KeypunchBlock.NAME_LIST_ID);
        if (names.isEmpty()) return;
        CatnipServices.NETWORK.sendToServer(
            new NameKeypunchC2SPacket(pos, names.get(minecraft.level.getRandom().nextInt(names.size()))));
    }
}
