package petrolpark.mc.destroy.compat.createbigcannons.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedProjectileBlock;

import petrolpark.mc.destroy.compat.createbigcannons.block.entity.CreateBigCannonBlockEntityTypes;
import petrolpark.mc.destroy.compat.createbigcannons.block.entity.CustomExplosiveMixShellBlockEntity;
import petrolpark.mc.destroy.compat.createbigcannons.entity.CreateBigCannonsEntityTypes;
import petrolpark.mc.destroy.compat.createbigcannons.entity.CustomExplosiveMixShellProjectile;

/**
 * Cannon shell whose payload is a Destroy {@code MixedExplosiveInventory} — detonates into
 * a {@code CustomExplosiveMixExplosion} on impact. Extends CBC's {@link FuzedProjectileBlock} so
 * the block inherits fuze-install behavior, cannon loading, and ground-detonation plumbing.
*/
public class CustomExplosiveMixShellBlock extends FuzedProjectileBlock<CustomExplosiveMixShellBlockEntity, CustomExplosiveMixShellProjectile> {

    public static final com.mojang.serialization.MapCodec<CustomExplosiveMixShellBlock> CODEC = simpleCodec(CustomExplosiveMixShellBlock::new);

    public CustomExplosiveMixShellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level level, BlockState state, BlockEntityType<S> type) {
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        withBlockEntityDo(level, pos, be -> be.onPlace(stack, level.registryAccess()));
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult dyeResult = onBlockEntityUse(level, pos, be -> be.tryDye(stack, hit, level, pos, player));
        if (dyeResult != InteractionResult.PASS) return ItemInteractionResult.sidedSuccess(level.isClientSide());
        // Delegate to CBC's fuze-install logic
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            withBlockEntityDo(level, pos, be -> serverPlayer.openMenu(be, be::writeToBuffer));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(be instanceof CustomExplosiveMixShellBlockEntity ebe)) return Collections.emptyList();
        HolderLookup.Provider provider = params.getLevel().registryAccess();
        List<ItemStack> drops = new ArrayList<>();
        drops.add(ebe.getFilledItemStack(CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_SHELL.asStack(), provider));
        if (!ebe.getFuze().isEmpty()) drops.add(ebe.getFuze().copy());
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof CustomExplosiveMixShellBlockEntity be)) return ItemStack.EMPTY;
        HolderLookup.Provider provider = (level instanceof Level lv) ? lv.registryAccess() : net.minecraft.core.RegistryAccess.EMPTY;
        return be.getFilledItemStack(CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_SHELL.asStack(), provider);
    }

    @Override
    public Class<CustomExplosiveMixShellBlockEntity> getBlockEntityClass() {
        return CustomExplosiveMixShellBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CustomExplosiveMixShellBlockEntity> getBlockEntityType() {
        return CreateBigCannonBlockEntityTypes.CUSTOM_EXPLOSIVE_MIX_SHELL.get();
    }

    @Override
    public AbstractBigCannonProjectile getProjectile(Level level, List<StructureBlockInfo> projectileBlocks) {
        CustomExplosiveMixShellProjectile projectile = CreateBigCannonsEntityTypes.CUSTOM_EXPLOSIVE_MIX_SHELL.get().create(level);
        if (projectile == null) return null;
        if (!projectileBlocks.isEmpty()) {
            StructureBlockInfo info = projectileBlocks.get(0);
            if (info.nbt() != null) {
                BlockEntity load = BlockEntity.loadStatic(info.pos(), info.state(), info.nbt(), level.registryAccess());
                if (load instanceof CustomExplosiveMixShellBlockEntity shell) {
                    projectile.color = shell.getColor();
                    projectile.setExplosiveInventory(shell.getExplosiveInventory());
                    projectile.setFuze(shell.getFuze().copy());
                }
            }
        }
        return projectile;
    }

    @Override
    public boolean isBaseFuze() {
        return CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE.getPropertiesOf(getAssociatedEntityType()).fuze().baseFuze();
    }

    @Override
    public EntityType<? extends CustomExplosiveMixShellProjectile> getAssociatedEntityType() {
        return CreateBigCannonsEntityTypes.CUSTOM_EXPLOSIVE_MIX_SHELL.get();
    }

    /** Expose CBC's inherited FACING (from DirectionalBlock) as a public constant for renderer / projectile.*/
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = DirectionalBlock.FACING;
}
