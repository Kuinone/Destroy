package petrolpark.mc.destroy.compat.createbigcannons.ponder;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import rbasamoyai.createbigcannons.CreateBigCannons;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.client.DestroyPonderTags;
import petrolpark.mc.destroy.compat.createbigcannons.block.CreateBigCannonsBlocks;
import petrolpark.mc.destroy.core.explosion.ExplosivesPonderScenes;

/**
 * CBC compat ponder storyboards — registers Custom Explosive Mix Charge under the Destroy
 * ponder tag with filling + dyeing scenes (referencing {@link ExplosivesPonderScenes}).
*/
public class CreateBigCannonsPonderScenes {

    private static PonderSceneRegistrationHelper<ResourceLocation> HELPER = null;
    @SuppressWarnings("unused")
    private static PonderSceneRegistrationHelper<ResourceLocation> CBC_HELPER = null;

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        HELPER = helper.withKeyFunction((s) -> ResourceLocation.tryParse(Destroy.MOD_ID));
        CBC_HELPER = helper.withKeyFunction((s) -> ResourceLocation.tryParse(CreateBigCannons.MOD_ID));

        HELPER.forComponents(CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_CHARGE.getId())
            .addStoryBoard("explosives/custom_explosive_mix_charge",
                (u, s) -> ExplosivesPonderScenes.filling(u, s, CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_CHARGE::asStack))
            .addStoryBoard("explosives/custom_explosive_mix_charge",
                (u, s) -> ExplosivesPonderScenes.dyeing(u, s, CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_CHARGE::asStack));
        // TODO: CBC — when CBC ponder MUNITIONS tag + CannonLoadingScenes are available, add:
        // CBC_HELPER.forComponents(CHARGE.getId()).addStoryBoard("munitions/cannon_loads", CannonLoadingScenes::cannonLoads, CBCPonderTags.MUNITIONS);
        // HELPER.forComponents(SHELL.getId()).addStoryBoard("explosives/custom_explosive_mix_shell", filling) + dyeing + exploding
        // CBC_HELPER.forComponents(SHELL.getId()).addStoryBoard("munitions/fuzing_munitions", CannonLoadingScenes::fuzingMunitions, CBCPonderTags.MUNITIONS);
    }

    public static void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(DestroyPonderTags.DESTROY)
            .item(CreateBigCannonsBlocks.CUSTOM_EXPLOSIVE_MIX_CHARGE);
        // TODO: CBC — Shell entry + CBC MUNITIONS tag entry deferred; add when CBC ponder API surface is audited for 1.21.
    }
}
