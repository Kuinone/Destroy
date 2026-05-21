package petrolpark.mc.destroy.core.mobeffect;

import petrolpark.mc.destroy.MoveToPetrolparkLibrary;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Base class for Destroy mob effects.
 *
 * TODO: reimplement the description-below-name tooltip using
 * {@link net.neoforged.neoforge.client.event.RenderTooltipEvent} or a Mixin on
 * {@code EffectRenderingInventoryScreen#renderTooltip}.
*/
@MoveToPetrolparkLibrary
public class DestroyMobEffect extends MobEffect {

    public DestroyMobEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }
}
