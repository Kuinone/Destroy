package petrolpark.mc.destroy.core.mobeffect;

import petrolpark.mc.destroy.MoveToPetrolparkLibrary;

import net.minecraft.world.effect.MobEffectCategory;

/**
 * 1.21.1 note: NeoForge removed {@code MobEffect.getCurativeItems}; curative items are now
 * driven by the {@code neoforge:cure_effects} data map on item level.
 *
 * TODO: Author cure-effects data map overrides where needed so that specific
 * items do NOT list instances of this effect. No override needed for plain uncurable effects.
*/
@MoveToPetrolparkLibrary
public class UncurableMobEffect extends DestroyMobEffect {

    public UncurableMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
