package petrolpark.mc.destroy.compat.createbigcannons.block.entity;

import com.tterrag.registrate.util.entry.BlockEntityEntry;

import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntityRenderer;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.compat.createbigcannons.block.CreateBigCannonsBlocks;

/**
 * Registers BE types for Destroy's two CBC-integration blocks. Shell gets CBC's Fuzed
 * visual/renderer; charge has no BE renderer (no moving parts).
*/
public class CreateBigCannonBlockEntityTypes {

    public static final BlockEntityEntry<CustomExplosiveMixChargeBlockEntity> CUSTOM_EXPLOSIVE_MIX_CHARGE =
        Destroy.REGISTRATE
            .blockEntity("custom_explosive_mix_charge", CustomExplosiveMixChargeBlockEntity::new)
            .validBlock(CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_CHARGE)
            .register();

    public static final BlockEntityEntry<CustomExplosiveMixShellBlockEntity> CUSTOM_EXPLOSIVE_MIX_SHELL =
        Destroy.REGISTRATE
            .blockEntity("custom_explosive_mix_shell", CustomExplosiveMixShellBlockEntity::new)
            // flywheel FuzedBlockVisual not wired (optional perf); FuzedBlockEntityRenderer
            // (BER path) handles fuze-out-of-shell rendering.
            .renderer(() -> FuzedBlockEntityRenderer::new)
            .validBlock(CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_SHELL)
            .register();

    public static void register() {
        // Class-load trigger.
    }
}
