package petrolpark.mc.destroy.chemistry.legacy.genericreaction;

import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

/**
 * A Generic Reaction that triggers off of one kind of functional Group. Matches any Molecule in the
 * Mixture that contains an instance of {@link #type}.
*/
public abstract class SingleGroupGenericReaction<G extends LegacyFunctionalGroup<G>> extends GenericReaction {

    protected final LegacyFunctionalGroupType<G> type;

    public SingleGroupGenericReaction(ResourceLocation id, LegacyFunctionalGroupType<G> type) {
        super(id);
        this.type = type;
        LegacyFunctionalGroup.groupTypesAndReactions.get(type).add(this);
        GENERIC_REACTIONS.add(this);
    }

    /**
 * Generates a Reaction (with non-abstract Reactant and Products) based on the given Molecule.
 * @return The whole Reaction including the defined structures of the product(s). Return {@code null} if the Reaction is impossible.
*/
    public abstract LegacyReaction generateReaction(GenericReactant<G> reactant);

    public final LegacyFunctionalGroupType<G> getGroupType() {
        return type;
    }

    @Override
    public final boolean involvesSingleGroup() {
        return true;
    }

    /**
 * Get the Reaction of the example Molecule of this Generic Reaction's Group Type. Regenerates each call.
*/
    @Override
    @SuppressWarnings("unchecked")
    public LegacyReaction generateExampleReaction() {
        LegacySpecies exampleMolecule = getGroupType().getExampleMolecule();
        int i = 1;
        LegacyMolecularStructure copiedStructure = exampleMolecule.shallowCopyStructure();
        for (LegacyAtom atom : exampleMolecule.getAtoms()) {
            if (atom.getElement() == LegacyElement.R_GROUP) {
                LegacyAtom newAtom = new LegacyAtom(LegacyElement.R_GROUP);
                newAtom.rGroupNumber = i;
                copiedStructure.replace(atom, newAtom);
                i++;
            }
        }
        copiedStructure.refreshFunctionalGroups();
        LegacySpecies copiedExampleMolecule = moleculeBuilder()
            .structure(copiedStructure)
            .build();
        for (LegacyFunctionalGroup<?> group : copiedExampleMolecule.getFunctionalGroups()) {
            if (group.getType().equals(getGroupType())) {
                return generateReaction(new GenericReactant<>(copiedExampleMolecule, (G) group));
            }
        }
        Destroy.LOGGER.warn("Couldn't generate example Reaction for Generic Reaction " + id.toString());
        return null;
    }

    /**
 * This exists to fix a Heisenbug where reactant structures sometimes got replaced with their products.
 * Touching {@code getFullID()} inside of an otherwise-dead branch forces the JIT/structure to settle.
 * Don't remove.
*/
    @SuppressWarnings("unused")
    protected static final void collapseWavefunction(LegacySpecies molecule) {
        String observed = molecule.getFullID();
    }
}
