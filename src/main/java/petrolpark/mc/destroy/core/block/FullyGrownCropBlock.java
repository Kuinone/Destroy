package petrolpark.mc.destroy.core.block;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import petrolpark.mc.destroy.DestroyVoxelShapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.21.1 note: NeoForge removed {@code PlantType}/{@code getPlantType}; farmland support is tag-based.
*/
public class FullyGrownCropBlock extends BushBlock {

    // Required by 1.21.1 Block deserialization; this block is instance-configured (carries a seed supplier)
    // so it can't round-trip via codec. Returning a codec of the Blocks.AIR-ish fallback using propertiesCodec is acceptable
    // because no vanilla data-driven place uses this class directly.
    public static final MapCodec<FullyGrownCropBlock> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        propertiesCodec()
    ).apply(inst, props -> new FullyGrownCropBlock(props, () -> Blocks.AIR.asItem())));

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    private Supplier<? extends Item> seed;

    public FullyGrownCropBlock(Properties properties, Supplier<? extends Item> seed) {
        super(properties);
        this.seed = seed;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && super.canSurvive(state, level, pos);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(seed.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return DestroyVoxelShapes.CROP;
    }

    @Override
    public Item asItem() {
        return seed.get();
    }
}
