package petrolpark.mc.destroy.content.oil.seismology;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.content.oil.seismology.SeismographItem.Seismograph;
import petrolpark.mc.destroy.content.oil.seismology.SeismographItem.Seismograph.Mark;

/**
 * Client → server packet: "the player clicked a grid cell on the Seismograph nonogram and wants
 * to change its mark". Server validates (held stack is Seismograph, mark isn't TICK/CROSS — those
 * are locked from player input and only set by the Seismometer trigger event), then writes the
 * new mark back to the stack's SEISMOGRAPH DataComponent.
*/
public record MarkSeismographC2SPacket(byte x, byte z, Mark mark, boolean mainHand)
    implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, MarkSeismographC2SPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BYTE, MarkSeismographC2SPacket::x,
            ByteBufCodecs.BYTE, MarkSeismographC2SPacket::z,
            CatnipStreamCodecBuilders.ofEnum(Mark.class), MarkSeismographC2SPacket::mark,
            ByteBufCodecs.BOOL, MarkSeismographC2SPacket::mainHand,
            MarkSeismographC2SPacket::new);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.MARK_SEISMOGRAPH;
    }

    @Override
    public void handle(ServerPlayer player) {
        ItemStack stack = player.getItemInHand(mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (mark != Seismograph.Mark.CROSS && mark != Seismograph.Mark.TICK && stack.getItem() instanceof SeismographItem) {
            Seismograph seismograph = SeismographItem.readSeismograph(stack);
            seismograph.mark(x, z, mark);
            seismograph.triggerSolveSeismographAdvancement(player.level(), player);
            SeismographItem.writeSeismograph(stack, seismograph);
        }
    }
}
