package petrolpark.mc.destroy.core.chemistry.storage;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Interface for blocks that provide a custom IFluidHandler to MixtureStorageItems via a
 * side+player+hand context (vs just a Direction). Blocks like the Vat expose phase-specific
 * (gas/liquid) tanks based on where the player clicks.
*/
public interface ISpecialMixtureContainerBlock {

    @Nullable
    IFluidHandler getTankForMixtureStorageItems(IMixtureStorageItem item, Level level, BlockPos pos,
                                                BlockState state, @Nullable Direction face,
                                                Player player, InteractionHand hand, ItemStack stack,
                                                boolean filling);
}
