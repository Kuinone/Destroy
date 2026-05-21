package petrolpark.mc.destroy.client;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.Destroy;

/**
 * {@link PonderPlugin} implementation — entry point for Destroy's Ponder scenes and tags. Plugin
 * registration happens via {@link net.createmod.ponder.foundation.PonderIndex#addPlugin
 * PonderIndex.addPlugin} at client mod-init (see
 * {@link petrolpark.mc.destroy.core.event.DestroyClientModEvents}). 1.21 Ponder's
 * {@code PonderPlugin} interface only requires {@link #getModId}; everything else has default
 * no-op implementations.
*/
public class DestroyPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return Destroy.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        DestroyPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        DestroyPonderTags.register(helper);
    }
}
