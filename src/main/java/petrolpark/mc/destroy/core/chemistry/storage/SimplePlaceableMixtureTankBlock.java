package petrolpark.mc.destroy.core.chemistry.storage;

import java.util.function.IntSupplier;

import org.joml.Vector3f;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.core.chemistry.storage.SimpleMixtureTankBlockEntity.SimplePlaceableMixtureTankBlockEntity;

/**
 * Concrete simple mixture tank block — takes capacity + fluid-box dimensions + voxel shape as
 * constructor parameters (pure data · no gameplay logic beyond what's in parent).
*/
public class SimplePlaceableMixtureTankBlock extends PlaceableMixtureTankBlock<SimplePlaceableMixtureTankBlockEntity> {

    /**
 * Block codec · 1.21 abstract-requirement. <b>Never actually invoked at runtime</b> in the
 * Destroy codebase — MC 1.21's structure serializer uses the registered block's ID
 * (destroy:beaker / flask / jar) to look up its exact singleton instance from
 * {@code BuiltInRegistries.BLOCK}, <b>not</b> this codec's decode path. The codec only gets
 * exercised if a downstream mod or tool serializes a Block via {@code Block.CODEC.encode}
 * directly, in which case the "Name"+"Properties" blockstate roundtrip suffices (Properties
 * map to blockstate IntegerProperty/BooleanProperty values · no ctor param info needed).
 *
 * <p>Implementation returns a ctor-compatible block with dummy capacity/dimensions/shape;
 * these fields are never read off the decoded block because the registry lookup returns the
 * real instance first. If you ever hit this code path, a warning is logged so we can add the
 * registry-lookup path.</p>
*/
    public static final MapCodec<SimplePlaceableMixtureTankBlock> CODEC =
        RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec()).apply(i,
            props -> {
                petrolpark.mc.destroy.Destroy.LOGGER.warn(
                    "SimplePlaceableMixtureTankBlock.CODEC.decode called — returning dummy fallback. " +
                    "Downstream code should use BuiltInRegistries.BLOCK.get(id) instead; file a bug if you see this.");
                return new SimplePlaceableMixtureTankBlock(props,
                    () -> 0,
                    Couple.create(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16)),
                    net.minecraft.world.phys.shapes.Shapes.block());
            }));

    protected final IntSupplier capacity;
    protected final Couple<Vector3f> fluidBoxDimensions;
    protected final VoxelShape shape;

    public SimplePlaceableMixtureTankBlock(Properties properties, IntSupplier capacity,
                                           Couple<Vector3f> fluidBoxDimensions, VoxelShape shape) {
        super(properties);
        this.capacity = capacity;
        this.fluidBoxDimensions = fluidBoxDimensions;
        this.shape = shape;
    }

    @Override
    protected MapCodec<? extends PlaceableMixtureTankBlock<SimplePlaceableMixtureTankBlockEntity>> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return shape;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SimpleMixtureTankBlockEntity tankBE) {
            return tankBE.luminosity;
        }
        return super.getLightEmission(state, level, pos);
    }

    @Override
    public int getMixtureCapacity() {
        return capacity.getAsInt();
    }

    public Couple<Vector3f> getFluidBoxDimensions() {
        return fluidBoxDimensions;
    }

    public static NonNullFunction<Properties, SimplePlaceableMixtureTankBlock> of(IntSupplier capacity,
                                                                                   float lx, float ly, float lz,
                                                                                   float ux, float uy, float uz,
                                                                                   VoxelShape shape) {
        return (p) -> new SimplePlaceableMixtureTankBlock(p, capacity,
            Couple.create(new Vector3f(lx, ly, lz), new Vector3f(ux, uy, uz)), shape);
    }

    @Override
    public Class<SimplePlaceableMixtureTankBlockEntity> getBlockEntityClass() {
        return SimplePlaceableMixtureTankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SimplePlaceableMixtureTankBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.SIMPLE_MIXTURE_TANK.get();
    }
}
