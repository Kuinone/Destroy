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
import petrolpark.mc.destroy.chemistry.legacy.index.group.CarbonylGroup;

public class WolffKishnerReduction extends SingleGroupGenericReaction<CarbonylGroup> {

    public WolffKishnerReduction() {
        super(Destroy.asResource("wolff_kishner_reduction"), DestroyGroupTypes.CARBONYL);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.HYDRAZINE)> 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<CarbonylGroup> reactant) {
        CarbonylGroup carbonyl = reactant.getGroup();
        LegacyMolecularStructure structure = reactant.getMolecule().shallowCopyStructure();

        structure.moveTo(carbonyl.carbon)
            .remove(carbonyl.oxygen)
            .addAtom(LegacyElement.HYDROGEN)
            .addAtom(LegacyElement.HYDROGEN);
        
        return reactionBuilder()
            .addReactant(reactant.getMolecule())
            .addReactant(DestroyMolecules.HYDRAZINE)
            .addCatalyst(DestroyMolecules.HYDROXIDE, 1)
            .addProduct(moleculeBuilder().structure(structure).build())
            .addProduct(DestroyMolecules.WATER)
            .addProduct(DestroyMolecules.NITROGEN)
            .build();
    };
    
};
