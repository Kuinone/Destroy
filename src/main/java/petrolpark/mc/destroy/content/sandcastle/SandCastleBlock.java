package petrolpark.mc.destroy.content.sandcastle;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroyVoxelShapes;

/**
 * Sand castle block — fragile decorative block built by {@link BucketAndSpadeItem} or by baby
 * Villagers via {@link BuildSandCastleGoal}. Comes in three material variants (sand / red sand /
 * soul sand) via the {@link #MATERIAL} blockstate property. Trampling or player-breaking triggers
 * the hosted {@link SentimentalBehaviour#onRemove} hook which may make the original builder cry.
*/
public class SandCastleBlock extends Block implements IBE<SandCastleBlockEntity> {

    public static final MapCodec<SandCastleBlock> CODEC = simpleCodec(SandCastleBlock::new);

    public static final EnumProperty<Material> MATERIAL = EnumProperty.create("material", Material.class);

    public SandCastleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(MATERIAL, Material.SAND));
    }

    @Override
    protected MapCodec<SandCastleBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MATERIAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DestroyVoxelShapes.SAND_CASTLE;
    }

    public static void setOwner(Level level, BlockPos pos, LivingEntity owner) {
        BlockEntityBehaviour.get(level, pos, SentimentalBehaviour.TYPE).setOwner(owner);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide() && entity.canTrample(state, pos, fallDistance)) {
            withBlockEntityDo(level, pos, be ->
                be.sentimentalBehaviour.onRemove(state, entity instanceof Player player ? player : null));
            level.destroyBlock(pos, false);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        withBlockEntityDo(level, pos, be -> be.sentimentalBehaviour.onRemove(state, player));
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    public static enum Material implements StringRepresentable {
        SAND, RED_SAND, SOUL_SAND;

        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }

    @Override
    public Class<SandCastleBlockEntity> getBlockEntityClass() {
        return SandCastleBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SandCastleBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.SAND_CASTLE.get();
    }
}
