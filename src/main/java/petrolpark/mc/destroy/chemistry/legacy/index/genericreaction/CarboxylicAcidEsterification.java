package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.DoubleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.AlcoholGroup;
import petrolpark.mc.destroy.chemistry.legacy.index.group.CarboxylicAcidGroup;

public class CarboxylicAcidEsterification extends DoubleGroupGenericReaction<CarboxylicAcidGroup, AlcoholGroup> {

    public CarboxylicAcidEsterification() {
        super(Destroy.asResource("carboxylic_acid_esterification"), DestroyGroupTypes.CARBOXYLIC_ACID, DestroyGroupTypes.ALCOHOL);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.OLEUM) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<CarboxylicAcidGroup> firstReactant, GenericReactant<AlcoholGroup> secondReactant) {
        LegacyMolecularStructure acidStructureCopy = firstReactant.getMolecule().shallowCopyStructure();
        CarboxylicAcidGroup acidGroup = firstReactant.getGroup();
        LegacyMolecularStructure alcoholStructureCopy = secondReactant.getMolecule().shallowCopyStructure();
        AlcoholGroup alcoholGroup = secondReactant.getGroup();

        alcoholStructureCopy.moveTo(alcoholGroup.oxygen);
        alcoholStructureCopy.remove(alcoholGroup.hydrogen);

        acidStructureCopy.moveTo(acidGroup.carbon)
            .remove(acidGroup.proton)
            .remove(acidGroup.alcoholOxygen);

        LegacySpecies ester = moleculeBuilder().structure(LegacyMolecularStructure.joinFormulae(acidStructureCopy, alcoholStructureCopy, BondType.SINGLE)).build();

        return reactionBuilder()
            .addReactant(firstReactant.getMolecule())
            .addReactant(secondReactant.getMolecule(), 1, 0)
            .addCatalyst(DestroyMolecules.SULFURIC_ACID, 1)
            .addProduct(ester)
            .addProduct(DestroyMolecules.WATER)
            // TODO rate constants
            .build();
    };
    
};
