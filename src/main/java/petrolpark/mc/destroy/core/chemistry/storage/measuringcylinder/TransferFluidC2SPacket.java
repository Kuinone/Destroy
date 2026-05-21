package petrolpark.mc.destroy.core.chemistry.storage.measuringcylinder;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.core.chemistry.storage.IMixtureStorageItem;
import petrolpark.mc.destroy.core.chemistry.storage.ItemMixtureTank;

/**
 * Client → server: "transfer N mB of fluid between the clicked block and my held mixture-storage
 * item". 1.21
*/
public record TransferFluidC2SPacket(BlockPos pos, Direction face, boolean mainHand,
                                     boolean blockToItem, int transferAmount)
    implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, TransferFluidC2SPacket> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, TransferFluidC2SPacket::pos,
            CatnipStreamCodecBuilders.ofEnum(Direction.class), TransferFluidC2SPacket::face,
            ByteBufCodecs.BOOL, TransferFluidC2SPacket::mainHand,
            ByteBufCodecs.BOOL, TransferFluidC2SPacket::blockToItem,
            ByteBufCodecs.VAR_INT, TransferFluidC2SPacket::transferAmount,
            TransferFluidC2SPacket::new);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.TRANSFER_FLUID;
    }

    @Override
    public void handle(ServerPlayer player) {
        InteractionHand hand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof IMixtureStorageItem mixtureItem)) return;

        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        IFluidHandlerItem cap = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (!(cap instanceof ItemMixtureTank itemTank)) return;
        IFluidHandler otherTank = mixtureItem.getTank(level, pos, state, face, player, hand, stack, !blockToItem);
        if (otherTank == null) return;

        if (blockToItem) {
            mixtureItem.afterFill(level, pos, state, face, player, hand, stack,
                mixtureItem.tryFill(stack, itemTank, otherTank, transferAmount));
        } else {
            mixtureItem.afterEmpty(level, pos, state, face, player, hand, stack,
                mixtureItem.tryEmpty(stack, itemTank, otherTank, false, transferAmount));
        }
    }
}
