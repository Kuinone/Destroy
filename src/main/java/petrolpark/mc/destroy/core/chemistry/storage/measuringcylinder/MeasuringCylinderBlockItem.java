package petrolpark.mc.destroy.core.chemistry.storage.measuringcylinder;

import org.joml.Vector3f;

import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import petrolpark.mc.destroy.core.block.IPickUpPutDownBlock;
import petrolpark.mc.destroy.core.chemistry.storage.IMixtureStorageItem;
import petrolpark.mc.destroy.core.chemistry.storage.ItemMixtureTank;
import petrolpark.mc.destroy.core.chemistry.storage.PlaceableMixtureTankItem;
import petrolpark.mc.destroy.core.chemistry.storage.SimpleMixtureTankRenderer.ISimpleMixtureTankRenderInformation;

/**
 1.21 migrations:
*/
public class MeasuringCylinderBlockItem extends PlaceableMixtureTankItem<MeasuringCylinderBlock>
    implements ISimpleMixtureTankRenderInformation<ItemStack> {

    public MeasuringCylinderBlockItem(MeasuringCylinderBlock block, Properties properties) {
        super(block, properties);
    }

    /**
 * Try opening the transfer screen on the client. Returns SUCCESS if a screen was launched,
 * PASS otherwise (caller can then fall back to default-place behaviour).
*/
    public static ItemInteractionResult tryOpenTransferScreen(Level level, BlockPos pos, BlockState state,
                                                              Direction face, Player player, InteractionHand hand,
                                                              ItemStack stack, boolean blockToItem) {
        if (!(stack.getItem() instanceof IMixtureStorageItem mixtureItem)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        IFluidHandler otherTank = mixtureItem.getTank(level, pos, state, face, player, hand, stack, blockToItem);
        if (otherTank == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        IFluidHandlerItem itemCap = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (!(itemCap instanceof ItemMixtureTank itemTank)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        int maxTransfer = blockToItem
            ? otherTank.drain(itemTank.getRemainingSpace(), FluidAction.SIMULATE).getAmount()
            : otherTank.fill(itemTank.getFluid(), FluidAction.SIMULATE);
        if (maxTransfer == 0) return ItemInteractionResult.FAIL;

        // Client-side screen open. — must dispatch via a Dist.CLIENT-only nested class
        // (and a dist guard) so server-side class verification of MeasuringCylinderBlockItem
        // doesn't drag in TransferFluidScreen (extends Screen, @OnlyIn(CLIENT)) — that triggers
        // a BootstrapMethodError on dedicated server during DestroyBlocks.<clinit>.
        if (level.isClientSide() && net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            ClientScreenOpener.open(pos, face, hand, maxTransfer, blockToItem);
        }
        return ItemInteractionResult.SUCCESS;
    }

    /** Resolved + class-loaded only on physical client.
 * References {@link TransferFluidScreen} freely; on dedicated server the JVM never loads
 * this nested class because the {@link #tryOpenTransferScreen} guard branches around it.
*/
    public static final class ClientScreenOpener {
        private ClientScreenOpener() {}

        public static void open(BlockPos pos, Direction face, InteractionHand hand,
                                int maxTransfer, boolean blockToItem) {
            net.createmod.catnip.gui.ScreenOpener.open(
                new TransferFluidScreen(pos, face, hand, maxTransfer, blockToItem));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemInteractionResult r = tryOpenTransferScreen(context.getLevel(), context.getClickedPos(),
            context.getLevel().getBlockState(context.getClickedPos()),
            context.getClickedFace(), context.getPlayer(), context.getHand(),
            context.getItemInHand(), false);
        if (r == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
            return place(new BlockPlaceContext(context));
        }
        return r.result();
    }

    // Initially added @SubscribeEvent onLeftClickBlock here, but realised it raced with
    // the existing handler in DestroyCommonEvents.onPlayerLeftClickBlock (which dispatches to
    // IMixtureStorageItem.defaultAttack — fill-everything semantics). Moved the cylinder-specific
    // transfer-screen branch into DestroyCommonEvents at so it runs *before* defaultAttack
    // and short-circuits with a transfer-screen open instead of an immediate fill. Keeping this
    // as a comment landmark for future debugging.

    // removed stale override that stripped the creative-mode branch from parent's place().
    // Parent PlaceableMixtureTankItem.place() already covers both creative + non-creative paths:
    // creative branch temp-flips Player.Abilities.instabuild so vanilla BlockItem.place actually
    // shrinks the held stack.

    @Override
    public Couple<Vector3f> getFluidBoxDimensions() {
        return MeasuringCylinderBlockEntity.FLUID_BOX_DIMENSIONS;
    }

    @Override
    public float getFluidLevel(ItemStack container, float partialTicks) {
        return getContents(container).map(fs -> (float) fs.getAmount()).orElse(0f) / getCapacity(container);
    }

    @Override
    public FluidStack getRenderedFluid(ItemStack container) {
        return getContents(container).orElse(FluidStack.EMPTY);
    }
}
