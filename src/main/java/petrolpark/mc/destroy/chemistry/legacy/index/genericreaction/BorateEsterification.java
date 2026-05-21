package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.DoubleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.AlcoholGroup;
import petrolpark.mc.destroy.chemistry.legacy.index.group.BoricAcidGroup;

public class BorateEsterification extends DoubleGroupGenericReaction<BoricAcidGroup, AlcoholGroup> {

    public BorateEsterification() {
        super(Destroy.asResource("borate_esterification"), DestroyGroupTypes.BORIC_ACID, DestroyGroupTypes.ALCOHOL);
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<BoricAcidGroup> firstReactant, GenericReactant<AlcoholGroup> secondReactant) {
        LegacyMolecularStructure boricAcid = firstReactant.molecule.shallowCopyStructure();
        LegacyMolecularStructure alcohol = secondReactant.molecule.shallowCopyStructure();
        boricAcid.moveTo(firstReactant.group.boron)
            .remove(firstReactant.group.hydrogen)
            .remove(firstReactant.group.oxygen);
        alcohol.moveTo(secondReactant.group.oxygen)
            .remove(secondReactant.group.hydrogen);
        LegacyMolecularStructure product = LegacyMolecularStructure.joinFormulae(boricAcid, alcohol, BondType.SINGLE);
        return reactionBuilder()
            .addReactant(firstReactant.molecule)
            .addReactant(secondReactant.molecule)
            .addProduct(moleculeBuilder().structure(product).build())
            .addProduct(DestroyMolecules.WATER)
            .displayAsReversible()
            .build();
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return true;
    };
    
};
