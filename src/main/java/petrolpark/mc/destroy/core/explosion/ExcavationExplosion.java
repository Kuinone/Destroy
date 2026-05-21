package petrolpark.mc.destroy.core.explosion;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import petrolpark.mc.destroy.config.DestroyAllConfigs;

/**
 * Flood-fill box excavation explosion centered on a position, bounded by an explicit
 * {@link AABB}. Unlike SmartExplosion's raycast/radius, this one just marks every reachable block
 * in the box for removal. Used by {@code DynamiteBlock}.
*/
public class ExcavationExplosion extends SmartExplosion {

    private final BlockPos center;
    private final AABB explosionArea;

    public ExcavationExplosion(Level level, Entity source, BlockPos position, AABB explosionArea) {
        super(level, source, null, null, VecHelper.getCenterOf(position), 0f, 0f);
        this.center = position;
        this.explosionArea = explosionArea;
    }

    @Override
    public ExplosionResult getExplosionResult() {
        Set<BlockPos> blocksToExplode = new HashSet<>();
        Set<BlockPos> blocksTriedToExplode = new HashSet<>();
        Queue<BlockPos> blocksToTryExplode = new LinkedList<>();
        blocksToTryExplode.add(center);

        if (DestroyAllConfigs.SERVER.blocks.dynamiteExplodesResistant.get()) {
            blocksToExplode.addAll(BlockPos.betweenClosedStream(explosionArea).toList());
        } else {
            // Flood fill within the explosion area so blocks protected by unbreakable walls are spared.
            while (!blocksToTryExplode.isEmpty()) {
                BlockPos pos = blocksToTryExplode.remove();
                if (shouldExplode(pos)) {
                    blocksToExplode.add(pos);
                    for (Direction direction : Direction.values()) {
                        BlockPos newPos = pos.relative(direction);
                        if (blocksTriedToExplode.contains(newPos)) continue;
                        blocksTriedToExplode.add(newPos);
                        blocksToTryExplode.add(newPos);
                    }
                }
            }
        }

        return new ExplosionResult(blocksToExplode, Map.of());
    }

    private boolean shouldExplode(BlockPos pos) {
        if (!level.isInWorldBounds(pos)) return false;
        if (!explosionArea.contains(VecHelper.getCenterOf(pos))) return false;
        return level.getBlockState(pos).getExplosionResistance(level, pos, this) < 1000f;
    }

    @Override
    public void effects(boolean spawnParticles) {
        if (level.isClientSide()) {
            level.playLocalSound(position.x, position.y, position.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS,
                4.0f, (1.0f + (random.nextFloat() * 0.4f)) * 0.7f, false);
        }
        if (spawnParticles && level instanceof ServerLevel serverLevel) {
            for (double x = explosionArea.minX; x < explosionArea.maxX; x += 3d) {
                for (double y = explosionArea.minY; y < explosionArea.maxY; y += 3d) {
                    for (double z = explosionArea.minZ; z < explosionArea.maxZ; z += 3d) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0d, 0d, 0d, 0.15d);
                    }
                }
            }
        }
    }

    @Override
    public void explodeBlock(BlockPos pos) {
        // Do nothing — this explosion does not drop block items
    }
}
