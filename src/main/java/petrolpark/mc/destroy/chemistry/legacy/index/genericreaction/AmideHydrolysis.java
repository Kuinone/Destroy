package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.UnsubstitutedAmideGroup;

public class AmideHydrolysis extends SingleGroupGenericReaction<UnsubstitutedAmideGroup> {
    
    public AmideHydrolysis() {
        super(Destroy.asResource("amide_hydrolysis"), DestroyGroupTypes.UNSUBSTITUTED_AMIDE);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.WATER) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<UnsubstitutedAmideGroup> reactant) {
        LegacyMolecularStructure structure = reactant.getMolecule().shallowCopyStructure();
        UnsubstitutedAmideGroup group = reactant.getGroup();

        structure.moveTo(group.carbon)
            .remove(group.hydrogen1)
            .remove(group.hydrogen2)
            .remove(group.nitrogen)
            .addGroup(LegacyMolecularStructure.alcohol());

        return reactionBuilder()
            .addReactant(reactant.getMolecule())
            .addReactant(DestroyMolecules.WATER)
            .addCatalyst(DestroyMolecules.PROTON, 1)
            .addProduct(moleculeBuilder().structure(structure).build())
            .addProduct(DestroyMolecules.AMMONIA)
            // TODO kinetics
            .build();
    };
};
