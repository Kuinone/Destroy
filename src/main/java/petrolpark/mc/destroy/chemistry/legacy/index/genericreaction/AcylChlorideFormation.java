package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.CarboxylicAcidGroup;

public class AcylChlorideFormation extends SingleGroupGenericReaction<CarboxylicAcidGroup> {

    public AcylChlorideFormation() {
        super(Destroy.asResource("acyl_chloride_formation"), DestroyGroupTypes.CARBOXYLIC_ACID);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.PHOSGENE) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<CarboxylicAcidGroup> reactant) {
        LegacySpecies reactantMolecule = reactant.getMolecule();
        CarboxylicAcidGroup acidGroup = reactant.getGroup();

        LegacySpecies productMolecule = moleculeBuilder().structure(reactantMolecule.shallowCopyStructure()
            .moveTo(acidGroup.carbon)
            .remove(acidGroup.alcoholOxygen)
            .remove(acidGroup.proton)
            .addAtom(LegacyElement.CHLORINE)
        ).build();

        return reactionBuilder()
            .addReactant(reactantMolecule)
            .addReactant(DestroyMolecules.PHOSGENE)
            .addProduct(productMolecule)
            .addProduct(DestroyMolecules.HYDROCHLORIC_ACID)
            .addProduct(DestroyMolecules.CARBON_DIOXIDE)
            // TODO kinetic constants
            .build();
    };
    
};
