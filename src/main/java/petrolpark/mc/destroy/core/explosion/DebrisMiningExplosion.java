package petrolpark.mc.destroy.core.explosion;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/**
 * 流体不作阻挡——爆炸直接穿过水/岩浆。不影响 entity 且不掉落方块物品。Cordite 专用。
 *
 * <p>Nested {@link FluidDestroyingDamageCalculator} 供 {@link UnderwaterExplosion} 继承复用。</p>
*/
public class DebrisMiningExplosion extends SmartExplosion {

    public static class FluidDestroyingDamageCalculator extends ExplosionDamageCalculator {

        @Override
        public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter reader, BlockPos pos,
                                                           BlockState state, FluidState fluid) {
            if (!fluid.isEmpty()) return Optional.of(0f);
            return state.isAir() ? Optional.empty()
                : Optional.of(state.getExplosionResistance(reader, pos, explosion));
        }
    }

    public DebrisMiningExplosion(Level level, Entity source, Vec3 position, float radius, float irregularity) {
        super(level, source, null, new FluidDestroyingDamageCalculator(), position, radius, irregularity);
    }

    @Override
    public void explodeEntity(Entity entity, float strength) {
        // Do nothing — this explosion does not affect entities
    }

    @Override
    public void explodeBlock(BlockPos pos) {
        // Do nothing — this explosion does not drop block items
    }
}
