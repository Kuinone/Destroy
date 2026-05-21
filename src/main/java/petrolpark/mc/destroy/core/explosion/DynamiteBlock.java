package petrolpark.mc.destroy.core.explosion;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;

/**
 * Area-excavation explosive block. Ignited by fire / redstone signal → triggers an
 * {@link ExcavationExplosion} in a cubic box around this block. Radius read from server config
 * {@code dynamiteMaxRadius}.
*/
public class DynamiteBlock extends Block implements IBE<DynamiteBlockEntity> {

    public DynamiteBlock(Properties properties) {
        super(properties);
    }

    /**
 * Build the excavation AABB from the BE's per-side scroll values.
 * Falls back to a single-block AABB if the BE isn't loaded yet (shouldn't normally happen
 * since the BE is created with the block).
*/
    public AABB excavationArea(Level level, BlockPos pos) {
        // 1.21 — `new AABB(BlockPos, BlockPos)` ctor removed; build from corner coords directly.
        return getBlockEntityOptional(level, pos)
            .map(be -> {
                BlockPos lo = be.excavationAreaLowerCorner;
                BlockPos hi = be.excavationAreaUpperCorner;
                return new AABB(lo.getX(), lo.getY(), lo.getZ(),
                    hi.getX() + 1, hi.getY() + 1, hi.getZ() + 1);
            })
            .orElseGet(() -> new AABB(pos));
    }

    public void explode(Level level, BlockPos pos, Entity source) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        SmartExplosion.explode(serverLevel, new ExcavationExplosion(level, source, pos, excavationArea(level, pos)));
    }

    @Override
    public Class<DynamiteBlockEntity> getBlockEntityClass() {
        return DynamiteBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DynamiteBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.DYNAMITE.get();
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos,
                             @Nullable Direction direction, @Nullable LivingEntity igniter) {
        super.onCaughtFire(state, level, pos, direction, igniter);
        explode(level, pos, igniter);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos)) {
            explode(level, pos, null);
        }
    }
}
