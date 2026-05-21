package petrolpark.mc.destroy.content.product.periodictable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Transparent "tank" variant of {@link PeriodicTableBlock} — a glass-like element block that
 * displays a colored fluid inside (color baked into {@link #color} at registration time). Used
 * for period-table elements that exist as fluids/liquids (e.g. Mercury, Bromine).
*/
public class TankPeriodicTableBlock extends PeriodicTableBlock {

    /**
 * Stub codec — never actually invoked in practice because Tank variants are registered via
 * Registrate with hardcoded {@code color} values. Codec 0xFFFFFF default is placeholder for
 * the {@code Block.codec()} registration-class serialization.
*/
    public static final MapCodec<TankPeriodicTableBlock> TANK_CODEC =
        simpleCodec(p -> new TankPeriodicTableBlock(p, 0xFFFFFF));

    public final int color;

    public TankPeriodicTableBlock(Properties properties, int color) {
        super(properties);
        this.color = color;
    }

    @Override
    protected MapCodec<? extends PeriodicTableBlock> codec() {
        return TANK_CODEC;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }
}
