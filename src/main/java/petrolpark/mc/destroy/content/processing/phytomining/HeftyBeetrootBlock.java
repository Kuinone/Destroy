package petrolpark.mc.destroy.content.processing.phytomining;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import petrolpark.mc.destroy.DestroyVoxelShapes;
import petrolpark.mc.destroy.core.block.FullyGrownCropBlock;

/**
 * The visually "bulbous" beetroot variant — overrides collision shape to match the sprite. Used
 * for the plain {@code HEFTY_BEETROOT} block and every {@code *_INFUSED_BEETROOT} variant; the
 * constructor's {@code seed} supplier is the item that drops / shows as pick-block.
*/
public class HeftyBeetrootBlock extends FullyGrownCropBlock {

    public static final MapCodec<HeftyBeetrootBlock> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        propertiesCodec()
    ).apply(inst, props -> new HeftyBeetrootBlock(props, () -> Blocks.AIR.asItem())));

    public HeftyBeetrootBlock(Properties properties, Supplier<? extends Item> seed) {
        super(properties, seed);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return DestroyVoxelShapes.HEFTY_BEETROOT;
    }
}
