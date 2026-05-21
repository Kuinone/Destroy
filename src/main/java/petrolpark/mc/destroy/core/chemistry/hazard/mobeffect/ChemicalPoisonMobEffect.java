package petrolpark.mc.destroy.core.chemistry.hazard.mobeffect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import petrolpark.mc.destroy.DestroyDamageSources;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.core.chemistry.hazard.EntityChemicalPoisonAttachment;
import petrolpark.mc.destroy.core.mobeffect.UncurableMobEffect;

/**
 * Uncurable chronic poisoning effect applied by {@code ChemistryHazardHelper.damage} when the entity
 * is exposed to an {@code ACUTELY_TOXIC}-tagged Molecule without sensitive-parts protection. Ticks
 * 1f periodic damage every 50 ticks; the damage is branded with the attached Molecule so the death
 * message reads "was poisoned by &lt;molecule name&gt;" instead of the generic {@code chemical_poison}.
*/
public class ChemicalPoisonMobEffect extends UncurableMobEffect {

    public ChemicalPoisonMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide()) {
            MobEffectInstance effect = livingEntity.getEffect(DestroyMobEffects.CHEMICAL_POISON.getDelegate());
            if (effect != null && effect.getDuration() % 50 == 0) {
                LegacySpecies molecule = livingEntity.getData(petrolpark.mc.destroy.DestroyAttachmentTypes.ENTITY_CHEMICAL_POISON).getMolecule();
                livingEntity.hurt(DestroyDamageSources.chemicalPoison(livingEntity.level(), molecule), 1f);
            }
        }
        return super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
