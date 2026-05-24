package petrolpark.mc.destroy.chemistry.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.api.error.ChemistryException;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;

/**
 * A Reaction takes place between specific {@link LegacySpecies Molecules}, and produces specific Molecules.
 * This is in contrast with {@link GenericReaction Generic Reactions}, which function as Reaction generators.
*/
public class LegacyReaction {

    public static final float GAS_CONSTANT = 8.3145f;

    /** The set of all Reactions known to Destroy, indexed by their IDs.*/
    public static final Map<String, LegacyReaction> REACTIONS = new HashMap<>();

    public static ReactionBuilder generatedReactionBuilder() {
        return new ReactionBuilder(new LegacyReaction("novel"), true, false);
    }

    private Map<LegacySpecies, Integer> reactants, products, orders;

    /** All Item Reactants (and catalysts) this Reaction.*/
    private List<IItemReactant> itemReactants;
    /** The number of moles of Reaction which will occur if all Item requirements are met.*/
    private float molesPerItem;

    /** Whether this Reaction needs UV light to proceed.*/
    private boolean isCatalysedByUV;

    /** The Reaction Result of this Reaction, if there is one.*/
    private ReactionResult result;

    // THERMODYNAMICS

    /** {@code A} in {@code k = Aexp(-E/RT)}.*/
    private float preexponentialFactor;
    /** {@code E} in {@code k = Aexp(-E/RT)}, in kJ/mol/s.*/
    private float activationEnergy;
    /** The change in enthalpy (in kJ/mol) for this Reaction.*/
    private float enthalpyChange;
    /** The half-cell potential of this Reaction under standard conditions.*/
    private float standardHalfCellPotential;
    /** If this is a half-cell reduction, this is how many electrons are on the left hand side.*/
    private int electrons;

    /** The namespace of the mod by which this Reaction was declared, or {@code "novel"} if generated.*/
    private String nameSpace;
    /** The ID of this reaction, not including its name space.*/
    private String id;
    /** Whether this Reaction was loaded from a datapack (vs. registered by Java at mod-init).
     * Datapack-loaded reactions are cleared and rebuilt every {@code /reload}; built-in ones persist.*/
    private boolean datapack;

    // JEI DISPLAY INFORMATION

    /** Whether this Reaction should be shown in JEI.*/
    private Supplier<Boolean> includeInJei;
    /** Whether this Reaction should use an equilibrium arrow when displayed in JEI.*/
    private boolean displayAsReversible;
    /** If this is the 'forward' half of a reversible Reaction, this points to the reverse Reaction.*/
    private LegacyReaction reverseReaction;

    /**
 * Get the Reaction with the given ID.
 * @param reactionId In the format {@code <namespace>:<id>}
 * @return {@code null} if no Reaction exists with that ID
*/
    public static LegacyReaction get(String reactionId) {
        return REACTIONS.get(reactionId);
    }

    protected LegacyReaction(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    /** Whether this Molecule gets consumed in this Reaction (does not include catalysts).*/
    public Boolean containsReactant(LegacySpecies molecule) {
        return this.reactants.keySet().contains(molecule);
    }

    /** Whether this Molecule is created in this Reaction.*/
    public Boolean containsProduct(LegacySpecies molecule) {
        return this.products.keySet().contains(molecule);
    }

    /** All Molecules which are consumed in this Reaction (but not their molar ratios).*/
    public Set<LegacySpecies> getReactants() {
        return this.reactants.keySet();
    }

    /** Whether this Reaction needs any Item Stack as a reactant.*/
    public boolean consumesItem() {
        for (IItemReactant itemReactant : itemReactants) {
            if (!itemReactant.isCatalyst()) return true;
        }
        return false;
    }

    /** Get the required Items for this Reaction.*/
    public List<IItemReactant> getItemReactants() {
        return itemReactants;
    }

    /** Get the moles of this Reaction that will occur once all Item requirements are fulfilled.*/
    public float getMolesPerItem() {
        return molesPerItem;
    }

    /** Whether this Reaction needs UV light to proceed.*/
    public boolean needsUV() {
        return isCatalysedByUV;
    }

    /** All Molecules which are created in this Reaction (but not their molar ratios).*/
    public Set<LegacySpecies> getProducts() {
        return this.products.keySet();
    }

    /** Get the activation energy for this Reaction, in kJ.*/
    public float getActivationEnergy() {
        return activationEnergy;
    }

    /** Get the preexponential factor for this Reaction, in mol/B/s.*/
    public float getPreexponentialFactor() {
        return preexponentialFactor;
    }

    /** The rate constant of this Reaction at the given temperature (kelvins).*/
    public float getRateConstant(float temperature) {
        return preexponentialFactor * (float) Math.exp(-((activationEnergy * 1000) / (GAS_CONSTANT * temperature)));
    }

    /** The enthalpy change for this Reaction, in kJ/mol.*/
    public float getEnthalpyChange() {
        return enthalpyChange;
    }

    /** Whether this Reaction has a Result.*/
    public boolean hasResult() {
        return result != null;
    }

    /** The Result of this Reaction, {@code null} if none.*/
    public ReactionResult getResult() {
        return result;
    }

    /** The unique identifier for this Reaction (not including its namespace).*/
    public String getId() {
        return id;
    }

    /** Get the fully unique ID for this Reaction, in the format {@code <namespace>:<id>}.*/
    public String getFullId() {
        return nameSpace + ":" + id;
    }

    /** Whether this Reaction should be displayed in the list of Reactions in JEI.*/
    public boolean includeInJei() {
        return includeInJei.get();
    }

    /** Whether this Reaction should be displayed in JEI with an equilibrium arrow.*/
    public boolean displayAsReversible() {
        return displayAsReversible;
    }

    /** If this is the 'forward' half of a reversible Reaction, gets the reverse Reaction.*/
    public Optional<LegacyReaction> getReverseReactionForDisplay() {
        return Optional.ofNullable(reverseReaction);
    }

    /** Return the Reaction which has an indexed Reaction Recipe that is displayed in JEI.*/
    public LegacyReaction getReactionDisplayedInJEI() {
        if (includeInJei.get()) return this;
        return getReverseReactionForDisplay().map(reaction -> reaction.includeInJei.get() ? reaction : null).orElse(null);
    }

    /** The name space of the mod by which this Reaction was defined.*/
    public String getNameSpace() {
        return nameSpace;
    }

    /** Whether this Reaction was loaded from a datapack (vs. a built-in Java registration).*/
    public boolean isDatapack() {
        return datapack;
    }

    /** Mark this Reaction as datapack-sourced. Called by the reload listener after build.*/
    public void markAsDatapack() {
        this.datapack = true;
    }

    /**
     * Remove every datapack-sourced reaction from {@link #REACTIONS} and clean up the per-species
     * reverse indexes ({@code reactantReactions} / {@code productReactions}). Called by the
     * reload listener before re-registering the new set.
     */
    public static void clearDatapackReactions() {
        Iterator<Entry<String, LegacyReaction>> it = REACTIONS.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, LegacyReaction> entry = it.next();
            LegacyReaction reaction = entry.getValue();
            if (!reaction.datapack) continue;
            for (LegacySpecies reactant : reaction.reactants.keySet()) {
                reactant.removeReactantReaction(reaction);
            }
            for (LegacySpecies product : reaction.products.keySet()) {
                product.removeProductReaction(reaction);
            }
            it.remove();
        }
    }

    /** Get the stoichometric ratio of this reactant or catalyst in this Reaction.*/
    public Integer getReactantMolarRatio(LegacySpecies reactant) {
        if (!reactants.keySet().contains(reactant)) {
            return 0;
        } else {
            return reactants.get(reactant);
        }
    }

    /** Get the stoichometric ratio of this product in this Reaction.*/
    public Integer getProductMolarRatio(LegacySpecies product) {
        if (!products.keySet().contains(product)) {
            return 0;
        } else {
            return products.get(product);
        }
    }

    /** Get every reactant and catalyst in this Reaction, mapped to their orders in the rate equation.*/
    public Map<LegacySpecies, Integer> getOrders() {
        return this.orders;
    }

    /** Get the standard half-cell potential of this reduction half-Reaction.*/
    public float getStandardHalfCellPotential() {
        return standardHalfCellPotential;
    }

    /** Get the electrons transferred in this reduction half-Reaction.*/
    public int getElectronsTransferred() {
        return electrons;
    }

    /** Whether this Reaction is a reduction half-Reaction.*/
    public boolean isHalfReaction() {
        return electrons != 0;
    }

    /**
 * A class for constructing Reactions.
*/
    public static class ReactionBuilder {

        private String namespace;

        /** Whether this Reaction is being generated by a Generic Reaction generator.*/
        private final boolean generated;
        private final LegacyReaction reaction;

        private final boolean declaredAsReverse;

        private boolean hasForcedPreExponentialFactor;
        private boolean hasForcedActivationEnergy;
        private boolean hasForcedEnthalpyChange;
        private boolean hasForcedHalfCellPotential;

        private ReactionBuilder(LegacyReaction reaction, boolean generated, boolean declaredAsReverse) {
            this.generated = generated;
            this.reaction = reaction;
            this.declaredAsReverse = declaredAsReverse;

            reaction.reactants = new HashMap<>();
            reaction.products = new HashMap<>();
            reaction.orders = new HashMap<>();

            reaction.itemReactants = new ArrayList<>();
            reaction.molesPerItem = 0f;

            reaction.includeInJei = () -> !generated && !declaredAsReverse;
            reaction.displayAsReversible = false;

            hasForcedPreExponentialFactor = false;
            hasForcedActivationEnergy = false;
            hasForcedEnthalpyChange = false;
            hasForcedHalfCellPotential = false;
        }

        public ReactionBuilder(String namespace) {
            this(new LegacyReaction(namespace), false, false);
            this.namespace = namespace;
        }

        private void checkNull(LegacySpecies molecule) {
            if (molecule == null) throw e("Molecules cannot be null");
        }

        /** Add a reactant of which one mole will be consumed per mole of Reaction.*/
        public ReactionBuilder addReactant(LegacySpecies molecule) {
            return addReactant(molecule, 1);
        }

        /** Add a reactant of which {@code ratio} moles will be consumed per mole of Reaction.*/
        public ReactionBuilder addReactant(LegacySpecies molecule, int ratio) {
            return addReactant(molecule, ratio, ratio);
        }

        /** Add a reactant with the given stoichometric ratio and rate-equation order.*/
        public ReactionBuilder addReactant(LegacySpecies molecule, int ratio, int order) {
            checkNull(molecule);
            reaction.reactants.put(molecule, ratio);
            reaction.orders.put(molecule, order);
            return this;
        }

        /** Sets the order of rate of Reaction of the given reactant.*/
        public ReactionBuilder setOrder(LegacySpecies molecule, int order) {
            if (!reaction.reactants.keySet().contains(molecule))
                throw e("Cannot modify order of a Molecule (" + molecule.getFullID() + ") that is not a reactant.");
            addCatalyst(molecule, order);
            return this;
        }

        /** Adds an Item Reactant (or catalyst) to this Reaction.*/
        public ReactionBuilder addItemReactant(IItemReactant itemReactant, float moles) {
            if (reaction.molesPerItem != 0f && reaction.molesPerItem != moles)
                throw e("The number of moles of Reaction which occur when all Item Requirements are met is constant for a Reaction, not individual per Item Reactant. The same number must be supplied each time an Item Reactant is added.");
            reaction.molesPerItem = moles;
            reaction.itemReactants.add(itemReactant);
            return this;
        }

        /** Adds an Item as a reactant for this Reaction.*/
        public ReactionBuilder addSimpleItemReactant(Supplier<Item> item, float moles) {
            return addItemReactant(new IItemReactant.SimpleItemReactant(item), moles);
        }

        /** Adds an Item Tag as a reactant for this Reaction.*/
        public ReactionBuilder addSimpleItemTagReactant(TagKey<Item> tag, float moles) {
            return addItemReactant(new IItemReactant.SimpleItemTagReactant(tag), moles);
        }

        /** Adds an Item as a catalyst for this Reaction.*/
        public ReactionBuilder addSimpleItemCatalyst(Supplier<Item> item, float moles) {
            return addItemReactant(new IItemReactant.SimpleItemCatalyst(item), moles);
        }

        /** Adds an Item Tag as a catalyst for this Reaction.*/
        public ReactionBuilder addSimpleItemTagCatalyst(TagKey<Item> tag, float moles) {
            return addItemReactant(new IItemReactant.SimpleItemTagCatalyst(tag), moles);
        }

        /** Set this Reaction as requiring ultraviolet light.*/
        public ReactionBuilder requireUV() {
            reaction.isCatalysedByUV = true;
            return this;
        }

        /** Add a product of which one mole will be produced per mole of Reaction.*/
        public ReactionBuilder addProduct(LegacySpecies molecule) {
            return addProduct(molecule, 1);
        }

        /** Add a product with the given stoichometric ratio.*/
        public ReactionBuilder addProduct(LegacySpecies molecule, int ratio) {
            checkNull(molecule);
            reaction.products.put(molecule, ratio);
            return this;
        }

        /** Add a catalyst (not consumed but affects rate).*/
        public ReactionBuilder addCatalyst(LegacySpecies molecule, int order) {
            checkNull(molecule);
            reaction.orders.put(molecule, order);
            return this;
        }

        /** Include this Reaction in JEI only if the condition is matched.*/
        public ReactionBuilder includeInJeiIf(Supplier<Boolean> condition) {
            reaction.includeInJei = condition;
            return this;
        }

        /** Don't include this Reaction in the list of Reactions shown in JEI.*/
        public ReactionBuilder dontIncludeInJei() {
            reaction.includeInJei = () -> false;
            return this;
        }

        /** Show a double-headed arrow for this Reaction in JEI.*/
        public ReactionBuilder displayAsReversible() {
            reaction.displayAsReversible = true;
            return this;
        }

        /** Set the ID for the Reaction.*/
        public ReactionBuilder id(String id) {
            reaction.id = id;
            return this;
        }

        /** Set the pre-exponential factor in the Arrhenius equation.*/
        public ReactionBuilder preexponentialFactor(float preexponentialFactor) {
            reaction.preexponentialFactor = preexponentialFactor;
            hasForcedPreExponentialFactor = true;
            return this;
        }

        /** Set the activation energy (in kJ).*/
        public ReactionBuilder activationEnergy(float activationEnergy) {
            reaction.activationEnergy = activationEnergy;
            hasForcedActivationEnergy = true;
            return this;
        }

        /** Set the enthalpy change (in kJ/mol).*/
        public ReactionBuilder enthalpyChange(float enthalpyChange) {
            reaction.enthalpyChange = enthalpyChange;
            hasForcedEnthalpyChange = true;
            return this;
        }

        /** Set the standard half-cell potential, in V.*/
        public ReactionBuilder standardHalfCellPotential(float standardHalfCellPotential) {
            if (hasForcedHalfCellPotential) throw e("Cannot set half-cell potential more than once.");
            reaction.standardHalfCellPotential = standardHalfCellPotential;
            hasForcedHalfCellPotential = true;
            return this;
        }

        /** Set the Reaction Result for this Reaction.*/
        public ReactionBuilder withResult(float moles, BiFunction<Float, LegacyReaction, ReactionResult> reactionresultFactory) {
            if (reaction.result != null) throw e("Reaction already has a Reaction Result. Use a CombinedReactionResult to have multiple.");
            reaction.result = reactionresultFactory.apply(moles, reaction);
            return this;
        }

        /**
 * Registers an acid. Auto-registers four Reactions (association + two dissociations with water/hydroxide).
*/
        public LegacyReaction acid(LegacySpecies acid, LegacySpecies conjugateBase, float pKa) {

            if (conjugateBase.getCharge() + 1 != acid.getCharge()) throw e("Acids must not violate the conservation of charge.");

            // Dissociation with water
            LegacyReaction dissociationReaction = this
                .id(acid.getFullID().split(":")[1] + ".dissociation")
                .addReactant(acid)
                .addCatalyst(DestroyMolecules.WATER, 1)
                .addProduct(DestroyMolecules.PROTON)
                .addProduct(conjugateBase)
                .activationEnergy(GAS_CONSTANT * 0.298f)
                .preexponentialFactor(0.5f * (float) Math.pow(10, -pKa))
                .dontIncludeInJei()
                .build();

            // Neutralization with hydroxide (temporary fix while API gets rewritten)
            new ReactionBuilder(namespace)
                .id(acid.getFullID().split(":")[1] + ".neutralization")
                .addReactant(acid)
                .addReactant(DestroyMolecules.HYDROXIDE)
                .addProduct(conjugateBase)
                .addProduct(DestroyMolecules.WATER)
                .activationEnergy(GAS_CONSTANT * 0.298f)
                .preexponentialFactor(0.5f * (float) Math.pow(10, -pKa))
                .dontIncludeInJei()
                .build();

            // Association
            new ReactionBuilder(namespace)
                .id(acid.getFullID().split(":")[1] + ".association")
                .addReactant(conjugateBase)
                .addReactant(DestroyMolecules.PROTON)
                .addProduct(acid)
                .activationEnergy(GAS_CONSTANT * 0.298f)
                .preexponentialFactor(1f)
                .dontIncludeInJei()
                .build();

            return dissociationReaction;
        }

        public ReactionBuilder reversible() {
            return reverseReaction(r -> {});
        }

        /**
 * Register a reverse Reaction for this Reaction.
*/
        public ReactionBuilder reverseReaction(Consumer<ReactionBuilder> reverseReactionModifier) {
            if (generated) throw e("Generated Reactions cannot be reversible. Add another Generic Reaction instead.");
            reaction.displayAsReversible = true;
            ReactionBuilder reverseBuilder = new ReactionBuilder(new LegacyReaction(namespace), false, true);
            for (Entry<LegacySpecies, Integer> reactant : reaction.reactants.entrySet()) {
                reverseBuilder.addProduct(reactant.getKey(), reactant.getValue());
            }
            for (Entry<LegacySpecies, Integer> product : reaction.products.entrySet()) {
                reverseBuilder.addReactant(product.getKey(), product.getValue());
            }
            for (Entry<LegacySpecies, Integer> rateAffecter : reaction.orders.entrySet()) {
                if (reaction.reactants.keySet().contains(rateAffecter.getKey())) continue;
                reverseBuilder.addCatalyst(rateAffecter.getKey(), rateAffecter.getValue());
            }
            reaction.reverseReaction = reverseBuilder.reaction;
            reverseBuilder.reaction.reverseReaction = reaction;

            reverseBuilder
                .id(reaction.id + ".reverse")
                .dontIncludeInJei();

            if (hasForcedEnthalpyChange) {
                reverseBuilder.enthalpyChange(-reaction.enthalpyChange);
                if (hasForcedActivationEnergy) {
                    reverseBuilder.activationEnergy(reaction.activationEnergy - reaction.enthalpyChange);
                }
            }

            if (reaction.needsUV()) reverseBuilder.requireUV();

            if (hasForcedHalfCellPotential) reverseBuilder.standardHalfCellPotential(-reaction.standardHalfCellPotential);

            reverseReactionModifier.accept(reverseBuilder);

            // Check thermodynamics are correct

            if (reaction.enthalpyChange != -reverseBuilder.reaction.enthalpyChange) {
                if (!hasForcedEnthalpyChange) {
                    enthalpyChange(reaction.activationEnergy - reverseBuilder.reaction.activationEnergy);
                    reverseBuilder.enthalpyChange(-reaction.enthalpyChange);
                } else {
                    throw e("The enthalpy change of a reverse reaction must be the negative of the forward");
                }
            }

            if (reaction.activationEnergy - reaction.enthalpyChange != reverseBuilder.reaction.activationEnergy) {
                if (!reverseBuilder.hasForcedActivationEnergy) {
                    reverseBuilder.activationEnergy(reaction.activationEnergy - reaction.enthalpyChange);
                } else if (!hasForcedActivationEnergy) {
                    activationEnergy(reverseBuilder.reaction.activationEnergy + reaction.enthalpyChange);
                } else {
                    throw e("Activation energies and enthalpy changes for reversible Reactions must obey Hess' Law");
                }
            }

            reverseBuilder.build();
            return this;
        }

        public LegacyReaction build() {

            if (reaction.id == null && !generated) {
                throw e("Reaction is missing an ID.");
            }

            // Electrochem calculations

            int chargeDecrease = 0;
            for (Entry<LegacySpecies, Integer> reactant : reaction.reactants.entrySet()) {
                chargeDecrease += reactant.getKey().getCharge() * reactant.getValue();
            }
            for (Entry<LegacySpecies, Integer> product : reaction.products.entrySet()) {
                chargeDecrease -= product.getKey().getCharge() * product.getValue();
            }
            if (chargeDecrease != 0 && chargeDecrease < 0 != declaredAsReverse) {
                throw e("Reactions must conserve charge or be reduction half-Reactions.");
            } else if (chargeDecrease == 0) {
                if (hasForcedHalfCellPotential) throw e("A half-cell potential is specified but electrons are not transferred.");
            } else {
                if (!hasForcedHalfCellPotential) throw e("Half-Reactions must specify a half-cell potential.");
                if (reaction.reverseReaction == null) throw e("Half-Reactions must be reversible.");
                reaction.electrons = chargeDecrease;
            }

            // Kinetics calculations

            if (!hasForcedActivationEnergy) {
                reaction.activationEnergy = 25f;
            }

            if (!hasForcedPreExponentialFactor || reaction.preexponentialFactor <= 0f) {
                reaction.preexponentialFactor = 1e4f;
            }

            if (!hasForcedEnthalpyChange) reaction.enthalpyChange = 0f;

            if (reaction.consumesItem() && reaction.molesPerItem == 0f) {
                Destroy.LOGGER.warn("Reaction '" + reactionString() + "' does not do anything when its required Items are consumed.");
            }

            // Overhead for built-in Reactions

            if (!generated) {
                for (LegacySpecies reactant : reaction.reactants.keySet()) {
                    reactant.addReactantReaction(reaction);
                }
                for (LegacySpecies product : reaction.products.keySet()) {
                    product.addProductReaction(reaction);
                }
                REACTIONS.put(reaction.getFullId(), reaction);
            }

            return reaction;
        }

        public boolean hasReactant(LegacySpecies reactant) {
            return reaction.reactants.containsKey(reactant);
        }

        public class ReactionConstructionException extends ChemistryException {

            public ReactionConstructionException(String message) {
                super(message);
            }
        }

        private ReactionConstructionException e(String message) {
            String id = reaction.id == null ? reactionString() : reaction.nameSpace + ":" + reaction.id;
            return new ReactionConstructionException("Problem generating reation (" + id + "): " + message);
        }

        private String reactionString() {
            StringBuilder reactionString = new StringBuilder();
            for (LegacySpecies reactant : reaction.reactants.keySet()) {
                reactionString.append(reactant.getSerlializedMolecularFormula(false));
                reactionString.append(" + ");
            }
            if (reaction.reactants.keySet().size() > 0) reactionString.setLength(reactionString.length() - 3);
            reactionString.append(" => ");
            int preProductsLen = reactionString.length();
            for (LegacySpecies product : reaction.products.keySet()) {
                reactionString.append(product.getSerlializedMolecularFormula(false));
                reactionString.append(" + ");
            }
            if (reaction.products.keySet().size() > 0) reactionString.setLength(reactionString.length() - 3);
            // guard: empty products shouldn't shave the " => " separator
            if (reactionString.length() < preProductsLen) reactionString.setLength(preProductsLen);
            return reactionString.toString();
        }
    }

    public static class RedoxReaction extends LegacyReaction {

        protected RedoxReaction(String nameSpace) {
            super(nameSpace);
        }
    }
}
