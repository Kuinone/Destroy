package petrolpark.mc.destroy.core.chemistry.vat;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.config.DestroyConfigs;

/**
 * Interface for blocks that can heat or cool a {@code Vat} (multi-block chemistry reactor). Also
 * consulted by Basins (see {@link petrolpark.mc.destroy.core.chemistry.basinreaction.ReactionInBasinRecipe
 * ReactionInBasinRecipe.create} — Basin reads its floor neighbour to pick up heating power).
 *
 * <ul>
 * <li>{@code DestroyAllConfigs.SERVER.blocks.blazeBurnerHeatingPower} → {@code DestroyConfigs.server().blocks.blazeBurnerHeatingPower}</li>
 * <li>{@code HeatLevel.FROSTING} — Create 1.21 may not have the FROSTING enum value (Refrigerant
 * Cooler extension), so we string-compare {@code heatLevel.name()} to be forward-compatible
 *</li>
 * <li>{@code BlazeBurnerBlock.HEAT_LEVEL} property path unchanged in Create 6.0.9</li>
 * </ul>
*/
public interface IVatHeaterBlock {

    /**
 * Get the power (in watts) the given Block State supplies or withdraws from a Vat.
 * @return Positive value for heaters, negative value for coolers
*/
    float getHeatingPower(Level level, BlockState blockState, BlockPos blockPos, Direction face);

    /**
 * Query the heating power of whatever block is at {@code blockPos} from the given {@code face}.
 * Handles three cases:
 * <ol>
 * <li>Air — returns 0</li>
 * <li>Block implementing {@link IVatHeaterBlock} — delegates to its instance method</li>
 * <li>Vanilla/Create blocks with {@code BlazeBurnerBlock.HEAT_LEVEL} property + face UP —
 * maps KINDLED/SEETHING/FROSTING to config-driven power values</li>
 * </ol>
*/
    public static float getHeatingPower(Level level, BlockPos blockPos, Direction face) {
        BlockState state = level.getBlockState(blockPos);

        if (state.isAir()) return 0f;

        // IVatHeaters
        if (state.getBlock() instanceof IVatHeaterBlock heater) {
            return heater.getHeatingPower(level, state, blockPos, face);
        }

        // Blaze Burners, Coolers, etc.
        if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL) && face == Direction.UP) {
            HeatLevel heatLevel = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
            if (heatLevel == HeatLevel.KINDLED) {
                return DestroyConfigs.server().blocks.blazeBurnerHeatingPower.getF();
            } else if (heatLevel == HeatLevel.SEETHING) {
                return DestroyConfigs.server().blocks.blazeBurnerSuperHeatingPower.getF();
            } else if ("FROSTING".equals(heatLevel.name())) {
                return DestroyConfigs.server().blocks.coolerHeatingPower.getF();
            }
        }

        return 0f;
    }
}
