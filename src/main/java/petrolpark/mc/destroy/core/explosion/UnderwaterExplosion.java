package petrolpark.mc.destroy.core.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import petrolpark.mc.destroy.core.explosion.DebrisMiningExplosion.FluidDestroyingDamageCalculator;

/**
 * Inversion of {@link DebrisMiningExplosion}: only solid blocks damaged, fluids pass through
 * undamaged. Useful for underwater demolition where you want to remove obstacles without
 * draining water columns. Picric Acid bomb uses this.
*/
public class UnderwaterExplosion extends SmartExplosion {

    public static class UnderwaterDamageCalculator extends FluidDestroyingDamageCalculator {

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos,
                                          BlockState state, float power) {
            return reader.getFluidState(pos).isEmpty();
        }
    }

    public UnderwaterExplosion(Level level, Entity source, Vec3 position, float radius, float irregularity) {
        super(level, source, null, new UnderwaterDamageCalculator(), position, radius, irregularity);
    }
}
