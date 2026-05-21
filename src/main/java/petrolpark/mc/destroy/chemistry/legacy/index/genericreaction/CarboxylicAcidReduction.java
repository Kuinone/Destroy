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
import petrolpark.mc.destroy.chemistry.legacy.index.group.CarboxylicAcidGroup;

public class CarboxylicAcidReduction extends SingleGroupGenericReaction<CarboxylicAcidGroup> {

    public CarboxylicAcidReduction() {
        super(Destroy.asResource("carboxylic_acid_reduction"), DestroyGroupTypes.CARBOXYLIC_ACID);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.BOROHYDRIDE) > 0f;
    };

    // TODO replace with some borane nonsense
    @Override
    public LegacyReaction generateReaction(GenericReactant<CarboxylicAcidGroup> reactant) {
        CarboxylicAcidGroup acid = reactant.getGroup();
        LegacyMolecularStructure structure = reactant.getMolecule().shallowCopyStructure();
        structure.moveTo(acid.carbon)
            .remove(acid.proton)
            .remove(acid.alcoholOxygen)
            .addAtom(LegacyElement.HYDROGEN);

        return reactionBuilder()
            .addReactant(reactant.getMolecule(), 4, 1)
            .addReactant(DestroyMolecules.BOROHYDRIDE)
            .addProduct(moleculeBuilder().structure(structure).build(), 4)
            .addProduct(DestroyMolecules.TETRAHYDROXYBORATE)
            .activationEnergy(25f)
            .build();
    };
    
};
