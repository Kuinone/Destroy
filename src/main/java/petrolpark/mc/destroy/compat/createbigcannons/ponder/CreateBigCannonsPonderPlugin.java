package petrolpark.mc.destroy.compat.createbigcannons.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.Destroy;

/**
 * Ponder plugin entry-point for the Create:Connected → Custom Explosive Mix Charge/Shell
 * compat scenes. Registered (when CBC is loaded) via
 * {@code PonderIndex.addPlugin(new CreateBigCannonsPonderPlugin())} from
 * {@link petrolpark.mc.destroy.compat.createbigcannons.CreateBigCannons#init} client-side.
*/
public class CreateBigCannonsPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return Destroy.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CreateBigCannonsPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        CreateBigCannonsPonderScenes.registerTags(helper);
    }
}
