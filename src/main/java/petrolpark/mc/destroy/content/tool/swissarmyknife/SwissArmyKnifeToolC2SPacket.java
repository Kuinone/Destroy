package petrolpark.mc.destroy.content.tool.swissarmyknife;

import javax.annotation.Nullable;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.content.tool.swissarmyknife.SwissArmyKnifeItem.Tool;

/**
 * Client → server packet: "the player's selected SwissArmyKnifeItem sub-tool has changed to X".
 * Server handler looks at both hands, finds any SwissArmyKnifeItem stack, and writes the
 * {@link petrolpark.mc.destroy.DestroyDataComponents#ACTIVE_TOOL} DataComponent to match.
*/
public record SwissArmyKnifeToolC2SPacket(@Nullable Tool tool) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, SwissArmyKnifeToolC2SPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SwissArmyKnifeToolC2SPacket::encodedOrdinal,
            SwissArmyKnifeToolC2SPacket::fromEncodedOrdinal);

    /** -1 encodes null, 0..n-1 encodes a Tool.values() index.*/
    public int encodedOrdinal() {
        return tool == null ? -1 : tool.ordinal();
    }

    private static SwissArmyKnifeToolC2SPacket fromEncodedOrdinal(int i) {
        if (i < 0) return new SwissArmyKnifeToolC2SPacket((Tool) null);
        Tool[] vals = Tool.values();
        if (i >= vals.length) return new SwissArmyKnifeToolC2SPacket((Tool) null);
        return new SwissArmyKnifeToolC2SPacket(vals[i]);
    }

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SWISS_ARMY_KNIFE_TOOL;
    }

    @Override
    public void handle(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof SwissArmyKnifeItem) {
                SwissArmyKnifeItem.putTool(stack, tool);
            }
        }
    }
}
