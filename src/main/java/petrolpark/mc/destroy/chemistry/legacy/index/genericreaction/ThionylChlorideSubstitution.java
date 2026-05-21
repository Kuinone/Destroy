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
import petrolpark.mc.destroy.chemistry.legacy.index.group.AlcoholGroup;

public class ThionylChlorideSubstitution extends SingleGroupGenericReaction<AlcoholGroup> {

    public ThionylChlorideSubstitution() {
        super(Destroy.asResource("thionyl_chloride_substitution"), DestroyGroupTypes.ALCOHOL);
    }

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.THIONYL_CHLORIDE) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<AlcoholGroup> reactant) {
        LegacyMolecularStructure structure = reactant.getMolecule().shallowCopyStructure();
        AlcoholGroup group = reactant.getGroup();

        structure.moveTo(group.carbon)
                .remove(group.oxygen)
                .remove(group.hydrogen)
                .addGroup(LegacyMolecularStructure.atom(LegacyElement.CHLORINE));

        return reactionBuilder()
                .addReactant(reactant.getMolecule())
                .addReactant(DestroyMolecules.THIONYL_CHLORIDE)
                .addProduct(moleculeBuilder().structure(structure).build())
                .addProduct(DestroyMolecules.HYDROCHLORIC_ACID)
                .addProduct(DestroyMolecules.SULFUR_DIOXIDE)
                // TODO kinetics
                .build();
    };
};