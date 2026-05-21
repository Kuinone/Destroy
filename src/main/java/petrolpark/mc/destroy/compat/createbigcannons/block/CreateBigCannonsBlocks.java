package petrolpark.mc.destroy.compat.createbigcannons.block;

import com.tterrag.registrate.util.entry.BlockEntry;

import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.munitions.config.MunitionPropertiesHandler;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.compat.createbigcannons.DestroyMunitionPropertiesHandlers;
import petrolpark.mc.destroy.compat.createbigcannons.item.CustomExplosiveMixChargeBlockItem;
import petrolpark.mc.destroy.compat.createbigcannons.item.CustomExplosiveMixShellBlockItem;

/**
 * Register Destroy's two CBC-integration blocks via Destroy's Registrate. Called from the
 * top-level {@code CreateBigCannons.init} under a {@code Mods.BIG_CANNONS.executeIfInstalled}
 * guard so the blocks only exist when CBC is on the classpath.
*/
public class CreateBigCannonsBlocks {

    public static final BlockEntry<CustomExplosiveMixChargeBlock> CUSTOM_EXPLOSIVE_MIX_CHARGE =
        Destroy.REGISTRATE.block("custom_explosive_mix_charge", CustomExplosiveMixChargeBlock::new)
            .initialProperties(CBCBlocks.POWDER_CHARGE)
            .properties(p -> p.noLootTable())
            .onRegister(block -> MunitionPropertiesHandler.registerBlockPropellantHandler(block, DestroyMunitionPropertiesHandlers.CUSTOM_EXPLOSIVE_MIX_CHARGE))
            .item(CustomExplosiveMixChargeBlockItem::new)
            .build()
            .register();

    public static final BlockEntry<CustomExplosiveMixShellBlock> CUSTOM_EXPLOSIVE_MIX_SHELL =
        Destroy.REGISTRATE.block("custom_explosive_mix_shell", CustomExplosiveMixShellBlock::new)
            .initialProperties(CBCBlocks.FLUID_SHELL)
            .properties(p -> p.noLootTable())
            .item(CustomExplosiveMixShellBlockItem::new)
            .build()
            .register();

    public static void register() {
        // Class-load trigger — field construction performs Registrate registration.
    }
}
