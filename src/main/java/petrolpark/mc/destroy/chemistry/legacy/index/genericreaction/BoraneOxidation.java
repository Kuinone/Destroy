package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.BoraneGroup;

public class BoraneOxidation extends SingleGroupGenericReaction<BoraneGroup> {

    public BoraneOxidation() {
        super(Destroy.asResource("borane_oxidation"), DestroyGroupTypes.BORANE);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.HYDROGEN_PEROXIDE) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<BoraneGroup> reactant) {
        LegacyMolecularStructure structure = reactant.getMolecule().shallowCopyStructure();
        structure.insertBridgingAtom(reactant.group.carbon, reactant.group.boron, new LegacyAtom(LegacyElement.OXYGEN));
        return reactionBuilder()
            .addReactant(reactant.molecule)
            .addReactant(DestroyMolecules.HYDROGEN_PEROXIDE)
            .addCatalyst(DestroyMolecules.HYDROXIDE, 1)
            .addProduct(moleculeBuilder().structure(structure).build())
            .addProduct(DestroyMolecules.WATER)
            .build();
    };
    
};
