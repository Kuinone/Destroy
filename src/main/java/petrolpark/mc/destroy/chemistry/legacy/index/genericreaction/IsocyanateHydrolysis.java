package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.IsocyanateGroup;

public class IsocyanateHydrolysis extends SingleGroupGenericReaction<IsocyanateGroup> {

    public IsocyanateHydrolysis() {
        super(Destroy.asResource("isocyanate_hydrolysis"), DestroyGroupTypes.ISOCYANATE);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.WATER) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<IsocyanateGroup> reactant) {
        IsocyanateGroup group = reactant.group;
        LegacyMolecularStructure structure = reactant.molecule.shallowCopyStructure();
        structure.moveTo(group.nitrogen)
            .remove(group.oxygen)
            .remove(group.functionalCarbon)
            .addAtom(LegacyElement.HYDROGEN)
            .addAtom(LegacyElement.HYDROGEN);
        return reactionBuilder()
            .addReactant(reactant.molecule)
            .addReactant(DestroyMolecules.WATER)
            .addProduct(DestroyMolecules.CARBON_DIOXIDE)
            .addProduct(moleculeBuilder().structure(structure).build())
            .build();
    };
    
};
