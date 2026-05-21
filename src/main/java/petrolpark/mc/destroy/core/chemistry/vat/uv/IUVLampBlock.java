package petrolpark.mc.destroy.core.chemistry.vat.uv;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.core.chemistry.vat.Vat;

/**
 * Interface for Blocks which can supply UV light to a {@link Vat}. Similar design to
 * {@link petrolpark.mc.destroy.core.chemistry.vat.IVatHeaterBlock IVatHeaterBlock}: static
 * {@link #getUVPower(Level, BlockPos, Direction)} resolver dispatches to per-block instance
 * method if the block implements this interface, otherwise returns 0 (no UV contribution).
*/
public interface IUVLampBlock {

    /**
 * Get the power (in watts) of ultraviolet light the given Block State supplies to a
 * {@link Vat}.
 *
 * @param level world
 * @param blockState state of the UV-emitting block
 * @param blockPos position of the UV-emitting block
 * @param face face of the block state touching the Vat
 * @return UV power in watts (0 if not applicable to this block/face)
*/
    float getUVPower(Level level, BlockState blockState, BlockPos blockPos, Direction face);

    /**
 * Static default-power resolver: dispatches to {@link IUVLampBlock#getUVPower} if the block
 * at {@code blockPos} implements this interface, otherwise returns 0.
*/
    public static float getUVPower(Level level, BlockPos blockPos, Direction face) {
        BlockState state = level.getBlockState(blockPos);

        if (state.isAir()) return 0f;

        if (state.getBlock() instanceof IUVLampBlock lamp) {
            return lamp.getUVPower(level, state, blockPos, face);
        }

        return 0f;
    }
}
