package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.AcylChlorideGroup;

public class AcylChlorideHydrolysis extends SingleGroupGenericReaction<AcylChlorideGroup> {

    public AcylChlorideHydrolysis() {
        super(Destroy.asResource("acyl_chloride_hydrolysis"), DestroyGroupTypes.ACYL_CHLORIDE);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.WATER) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<AcylChlorideGroup> reactant) {
        LegacyMolecularStructure structure = reactant.getMolecule().shallowCopyStructure();
        AcylChlorideGroup group = reactant.getGroup();

        structure.moveTo(group.getCarbon())
            .remove(group.getChlorine())
            .addGroup(LegacyMolecularStructure.alcohol(), true);

        return reactionBuilder()
            .addReactant(reactant.getMolecule())
            .addReactant(DestroyMolecules.WATER)
            .addProduct(moleculeBuilder().structure(structure).build())
            .addProduct(DestroyMolecules.HYDROCHLORIC_ACID)
            // TODO kinetics
            .build();
    };

};
