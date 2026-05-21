package petrolpark.mc.destroy.core.chemistry.hazard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyDamageSources;
import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.DestroyTags;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.config.DestroySubstancesConfigs;
import petrolpark.mc.destroy.core.chemistry.hazard.mobeffect.CryingMobEffect;
import petrolpark.mc.destroy.core.pollution.PollutionHelper;

/**
 * Central handler for chemistry-driven hazards: Mixture exposure damage, item contamination, cancer
 * tick from UV exposure under ozone depletion, eating-prevention when wearing BB withdrawal + more.
 *
 * <p>Fragrance effect path: {@code le.hasEffect(DestroyMobEffects.FRAGRANCE.get())} works because
 * {@link DestroyMobEffects} entries expose a {@code get() → MobEffect} API; 1.21 {@code hasEffect}
 * now takes {@code Holder<MobEffect>}, hence {@code getDelegate()} accessor for the Holder.</p>
*/
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class ChemistryHazardHelper {

    /**
 * Apply the effects of exposure to a Mixture to an Entity.
 * @param level world reference (damage source + advancement dispatch)
 * @param entity exposed entity
 * @param stack fluid stack — no-op unless {@link DestroyFluids#isMixture(FluidStack)}
 * @param skinContact {@code true} for whole-body submersion (vs mouth/face only); gates
 * acid-burn damage + armor contamination
*/
    public static void damage(Level level, LivingEntity entity, FluidStack stack, boolean skinContact) {
        if (!DestroyFluids.isMixture(stack)) return;
        ReadOnlyMixture mixture = ReadOnlyMixture.readNBT(ReadOnlyMixture::new,
            stack.getOrDefault(DestroyDataComponents.MIXTURE, new net.minecraft.nbt.CompoundTag()));
        if (mixture.isEmpty()) return;

        boolean burning = mixture.getConcentrationOf(DestroyMolecules.PROTON) > 0.01f
            || mixture.getConcentrationOf(DestroyMolecules.HYDROXIDE) > 0.01f;
        boolean smelly = false;
        boolean carcinogen = false;
        boolean lacrimator = false;
        boolean lead = false;
        LegacySpecies toxicMolecule = null;

        for (LegacySpecies molecule : mixture.getContents(true)) {
            if (molecule.hasTag(DestroyMolecules.Tags.ACUTELY_TOXIC)) toxicMolecule = molecule;
            if (molecule.hasTag(DestroyMolecules.Tags.SMELLY))        smelly = true;
            if (molecule.hasTag(DestroyMolecules.Tags.CARCINOGEN))    carcinogen = true;
            if (molecule.hasTag(DestroyMolecules.Tags.LACRIMATOR))    lacrimator = true;
            if (molecule.getMolecularFormula().containsKey(LegacyElement.LEAD)) lead = true;
            if (toxicMolecule != null && smelly && carcinogen && lacrimator && lead) break;
        }

        boolean noseProtected  = Protection.NOSE.isProtected(entity);
        boolean mouthProtected = Protection.MOUTH.isProtected(entity);
        boolean eyesProtected  = Protection.EYES.isProtected(entity);
        boolean sensitivePartsProtected = noseProtected && mouthProtected && eyesProtected;
        boolean wholeBodyProtected = sensitivePartsProtected
            && Protection.HEAD.isProtected(entity)
            && Protection.BODY.isProtected(entity)
            && Protection.LEGS.isProtected(entity)
            && Protection.FEET.isProtected(entity);

        // Wah wah cry like a little baby
        if (lacrimator && !eyesProtected) {
            entity.addEffect(new MobEffectInstance(DestroyMobEffects.CRYING.getDelegate(), 600, 0, false, false, true));
            // wire up the immediate-render broadcast. Vanilla MobEffect sync is ~1 tick late; this packet ensures
            // tears render immediately when chemistry exposure starts the cry. See
            CryingMobEffect.broadcastCryingStarted(entity);
        }

        // Smelly chemicals
        if (smelly && !noseProtected && !(entity instanceof Player player && player.isCreative())) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false));
        }

        // Acutely toxic Molecules
        if (toxicMolecule != null && !sensitivePartsProtected && !level.isClientSide()) {
            EntityChemicalPoisonAttachment.setMolecule(entity, toxicMolecule);
            if (!entity.hasEffect(DestroyMobEffects.CHEMICAL_POISON.getDelegate()))
                entity.addEffect(new MobEffectInstance(DestroyMobEffects.CHEMICAL_POISON.getDelegate(), 219, 0, false, false));
        }

        // Carcinogens
        if (carcinogen && (!noseProtected || !mouthProtected)) {
            if (entity.getRandom().nextInt(2400) == 0) entity.addEffect(DestroyMobEffects.cancerInstance());
        }

        // Lead poisoning
        if (lead && (!noseProtected || !mouthProtected)) {
            if (entity.getRandom().nextInt(2400) == 0)
                DestroyMobEffects.increaseEffectLevel(entity, DestroyMobEffects.LEAD_POISONING.getDelegate(), 1, -1);
        }

        // Acid/base burns
        if (skinContact) {
            if (wholeBodyProtected && (burning || smelly || carcinogen || toxicMolecule != null)) {
                // Armor contaminated — will re-damage when unequipped (see onLivingEquipmentChange)
                for (ItemStack armor : entity.getArmorSlots()) contaminate(armor, stack);
            } else {
                if (burning) {
                    entity.hurt(DestroyDamageSources.chemicalBurn(level), 5f);
                }
            }
        }
    }

    /**
 * Attach a {@link FluidStack} to an item stack as a "contaminating fluid" marker. Consumed by
 * {@link #onLivingEquipmentChange} to re-apply damage when the contaminated armor is unequipped
 * without prior decontamination.
*/
    public static void contaminate(ItemStack stack, FluidStack fluidStack) {
        if (!DestroyTags.Items.CONTAMINABLE.matches(stack.getItem())) return;
        if (stack.has(DestroyDataComponents.CONTAMINATING_FLUID)) return; // don't replace existing
        stack.set(DestroyDataComponents.CONTAMINATING_FLUID, fluidStack.copy());
    }

    /** Strip the {@code CONTAMINATING_FLUID} DataComponent from an item stack.*/
    public static void decontaminate(ItemStack stack) {
        stack.remove(DestroyDataComponents.CONTAMINATING_FLUID);
    }

    /**
 * Body-part coverage categories. Each enum value collects {@link Predicate}s over a
 * {@link LivingEntity} — {@link #isProtected(LivingEntity)} passes if any predicate matches.
 * Default test uses the vanilla equipment slot + a Destroy item tag; Curios wires extra
 * predicates via {@code DestroyCurios.init}.
*/
    public static enum Protection {
        FEET(EquipmentSlot.FEET,  DestroyTags.Items.CHEMICAL_PROTECTION_FEET),
        LEGS(EquipmentSlot.LEGS,  DestroyTags.Items.CHEMICAL_PROTECTION_LEGS),
        BODY(EquipmentSlot.CHEST, DestroyTags.Items.CHEMICAL_PROTECTION_CHEST),
        HEAD(EquipmentSlot.HEAD,  DestroyTags.Items.CHEMICAL_PROTECTION_HEAD),
        EYES(EquipmentSlot.HEAD,  DestroyTags.Items.CHEMICAL_PROTECTION_EYES),
        NOSE(EquipmentSlot.HEAD,  DestroyTags.Items.CHEMICAL_PROTECTION_NOSE),
        MOUTH(EquipmentSlot.HEAD, DestroyTags.Items.CHEMICAL_PROTECTION_MOUTH),
        /** Whether the mouth is obstructed, preventing things that require an open mouth like eating.*/
        MOUTH_COVERED(EquipmentSlot.HEAD, DestroyTags.Items.CHEMICAL_PROTECTION_MOUTH);

        public final DestroyTags.Items defaultTag;

        private final List<Predicate<LivingEntity>> tests = new ArrayList<>();

        public void registerTest(Predicate<LivingEntity> testForProtection) {
            tests.add(testForProtection);
        }

        public boolean isProtected(LivingEntity livingEntity) {
            return tests.stream().anyMatch(t -> t.test(livingEntity));
        }

        private Protection(EquipmentSlot defaultEquipmentSlot, DestroyTags.Items defaultTag) {
            this.defaultTag = defaultTag;
            registerTest(le -> defaultTag.matches(le.getItemBySlot(defaultEquipmentSlot).getItem()));
        }

        static {
            // Fragrance effect masks smells regardless of physical nose protection.
            NOSE.registerTest(le -> le.hasEffect(DestroyMobEffects.FRAGRANCE.getDelegate()));
        }
    }

    /**
 * Give cancer if unprotected in direct sunlight with high ozone depletion. Probability scales
 * with {@code OZONE_DEPLETION} pollution level.
*/
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().canSeeSky(player.blockPosition())) return;
        if (player.hasEffect(DestroyMobEffects.SUN_PROTECTION.getDelegate())) return;
        int ozonePollution = PollutionHelper.getPollution(player.level(), DestroyPollutionTypes.OZONE_DEPLETION.get());
        int ozoneMax = PollutionHelper.getLevelPollutionTypeProperties(DestroyPollutionTypes.OZONE_DEPLETION.get()).max();
        if (player.getRandom().nextInt(ozoneMax * 600) < ozonePollution) {
            player.addEffect(DestroyMobEffects.cancerInstance());
        }
    }

    /** Prevent eating if Baby Blue withdrawal is active + the item isn't safe-fallback food.*/
    @SubscribeEvent
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();

        if (!stack.has(DataComponents.FOOD)) return;
        if (!DestroySubstancesConfigs.babyBlueEnabled()) return;
        if (stack.getItem() == DestroyItems.BABY_BLUE_POWDER.get()) return;
        if (!player.hasEffect(DestroyMobEffects.BABY_BLUE_WITHDRAWAL.getDelegate())) return;

        FoodProperties food = stack.getFoodProperties(player);
        if (food != null && food.canAlwaysEat()) return;

        player.displayClientMessage(DestroyLang.translate("tooltip.eating_prevented.baby_blue").component(), true);
        event.setCanceled(true);
    }

    /**
 * Damage entities with the effects of chemicals if they take off contaminated armor without
 * washing it first.
*/
    @SubscribeEvent
    public static final void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        EquipmentSlot slot = event.getSlot();
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) return;
        ItemStack from = event.getFrom();
        FluidStack contaminating = from.get(DestroyDataComponents.CONTAMINATING_FLUID);
        if (contaminating == null || contaminating.isEmpty()) return;
        damage(event.getEntity().level(), event.getEntity(), contaminating, true);
        decontaminate(from);
    }
}
