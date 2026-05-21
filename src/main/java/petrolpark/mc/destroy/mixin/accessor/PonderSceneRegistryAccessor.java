package petrolpark.mc.destroy.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.common.collect.Multimap;

import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.foundation.registration.PonderSceneRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * Exposes the private {@code scenes} {@link Multimap} field of Ponder-NeoForge's
 * {@link PonderSceneRegistry} for runtime mutation. Used by
 * {@link petrolpark.mc.destroy.client.DestroyPonderScenes#refreshPeriodicTableBlockScenes
 * DestroyPonderScenes#refreshPeriodicTableBlockScenes} to dispatch the {@code periodic_table}
 * Ponder story-board onto every element block (122 entries) after data-pack reload.
 *
 * <p><b>Why not a public API</b>: 1.21 Ponder-NeoForge's {@link
 * net.createmod.ponder.api.registration.SceneRegistryAccess SceneRegistryAccess} interface is
 * read-only ({@code doScenesExistForId / getRegisteredEntries / compile}). No public method
 * exists to mutate the underlying scene multimap. This Accessor mixin is the only sanctioned
 * Mixin-based escape hatch.</p>
*/
@Mixin(PonderSceneRegistry.class)
public interface PonderSceneRegistryAccessor {

    @Accessor(value = "scenes", remap = false)
    Multimap<ResourceLocation, StoryBoardEntry> getScenes();
}
