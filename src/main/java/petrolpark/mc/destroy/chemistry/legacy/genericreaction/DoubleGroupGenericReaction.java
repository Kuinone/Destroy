package petrolpark.mc.destroy.chemistry.legacy.genericreaction;

import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

/**
 * A Generic Reaction that triggers off of two different kinds of functional Group (e.g. acyl chloride
 * + amine → amide). Matches a pair of Molecules in the Mixture where one contains {@link #firstType}
 * and the other contains {@link #secondType}.
*/
public abstract class DoubleGroupGenericReaction<FirstGroup extends LegacyFunctionalGroup<FirstGroup>,
                                                 SecondGroup extends LegacyFunctionalGroup<SecondGroup>>
    extends GenericReaction {

    protected final LegacyFunctionalGroupType<FirstGroup> firstType;
    protected final LegacyFunctionalGroupType<SecondGroup> secondType;

    /** This number is used to number R Groups in example Reactions.*/
    private int i;

    public DoubleGroupGenericReaction(ResourceLocation id,
                                      LegacyFunctionalGroupType<FirstGroup> firstType,
                                      LegacyFunctionalGroupType<SecondGroup> secondType) {
        super(id);
        this.firstType = firstType;
        this.secondType = secondType;
        LegacyFunctionalGroup.groupTypesAndReactions.get(firstType).add(this);
        LegacyFunctionalGroup.groupTypesAndReactions.get(secondType).add(this);
        GENERIC_REACTIONS.add(this);
    }

    /**
 * Generates a Reaction based on the given pair of Molecules.
 * @param firstReactant has the first declared Group.
 * @param secondReactant has the second declared Group.
 * @return The whole Reaction including the defined structures of the product(s). Return {@code null} if impossible.
*/
    public abstract LegacyReaction generateReaction(GenericReactant<FirstGroup> firstReactant,
                                                    GenericReactant<SecondGroup> secondReactant);

    @Override
    public final boolean involvesSingleGroup() {
        return false;
    }

    public final LegacyFunctionalGroupType<FirstGroup> getFirstGroupType() {
        return firstType;
    }

    public final LegacyFunctionalGroupType<SecondGroup> getSecondGroupType() {
        return secondType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LegacyReaction generateExampleReaction() {

        i = 1;
        LegacySpecies exampleMolecule1 = copyAndNumberRGroups(getFirstGroupType().getExampleMolecule());
        LegacySpecies exampleMolecule2 = copyAndNumberRGroups(getSecondGroupType().getExampleMolecule());

        GenericReactant<FirstGroup> reactant1 = null;
        GenericReactant<SecondGroup> reactant2 = null;

        for (LegacyFunctionalGroup<?> group : exampleMolecule1.getFunctionalGroups()) {
            if (group.getType() == getFirstGroupType()) reactant1 = new GenericReactant<>(exampleMolecule1, (FirstGroup) group);
        }
        for (LegacyFunctionalGroup<?> group : exampleMolecule2.getFunctionalGroups()) {
            if (group.getType() == getSecondGroupType()) reactant2 = new GenericReactant<>(exampleMolecule2, (SecondGroup) group);
        }

        if (reactant1 == null || reactant2 == null)
            throw new IllegalStateException("Couldn't generate example Reaction for Generic Reaction " + id.toString());

        return generateReaction(reactant1, reactant2);
    }

    private LegacySpecies copyAndNumberRGroups(LegacySpecies molecule) {
        LegacyMolecularStructure copiedStructure = molecule.shallowCopyStructure();
        for (LegacyAtom atom : molecule.getAtoms()) {
            if (atom.getElement() == LegacyElement.R_GROUP) {
                LegacyAtom newAtom = new LegacyAtom(LegacyElement.R_GROUP);
                newAtom.rGroupNumber = i;
                copiedStructure.replace(atom, newAtom);
                i++;
            }
        }
        return moleculeBuilder()
            .structure(copiedStructure)
            .build();
    }
}
