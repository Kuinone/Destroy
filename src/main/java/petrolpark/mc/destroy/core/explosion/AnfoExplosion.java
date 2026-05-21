package petrolpark.mc.destroy.core.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import petrolpark.mc.destroy.DestroyTags;

/**
 * 仅破坏空气、流体、"gangue"（杂矿石）——不影响玩家 ore 方块；knockback/damage 对 entity 降至 30%。
 * 矿工用的受控爆破。
*/
public class AnfoExplosion extends SmartExplosion {

    public static class NaturalBlockOnlyDamageCalculator extends ExplosionDamageCalculator {

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
            return (!reader.getFluidState(pos).isEmpty() || state.is(DestroyTags.Blocks.GANGUE.tag))
                && !state.is(Tags.Blocks.ORES);
        }
    }

    public AnfoExplosion(Level level, Entity source, Vec3 position, float radius, float irregularity) {
        super(level, source, null, new NaturalBlockOnlyDamageCalculator(), position, radius, irregularity);
    }

    @Override
    public void explodeEntity(Entity entity, float strength) {
        super.explodeEntity(entity, strength * 0.3f);
    }

    @Override
    public void explodeBlock(BlockPos pos) {
        // Do nothing — this explosion does not drop block items
    }
}
