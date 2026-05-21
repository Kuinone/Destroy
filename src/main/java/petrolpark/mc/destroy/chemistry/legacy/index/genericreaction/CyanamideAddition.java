package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.NonTertiaryAmineGroup;

public class CyanamideAddition extends SingleGroupGenericReaction<NonTertiaryAmineGroup> {

    public CyanamideAddition() {
        super(Destroy.asResource("cyanamide_addition"), DestroyGroupTypes.NON_TERTIARY_AMINE);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.CYANAMIDE) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<NonTertiaryAmineGroup> reactant) {
        LegacyMolecularStructure structure = reactant.molecule.shallowCopyStructure();
        structure.moveTo(reactant.group.nitrogen)
            .remove(reactant.group.hydrogen)
            .addGroup(LegacyMolecularStructure.atom(LegacyElement.CARBON), false)
            .addAtom(LegacyElement.NITROGEN)
            .addAtom(LegacyElement.NITROGEN, BondType.DOUBLE);

        if(reactant.molecule.isHypothetical())
            structure.addAllHydrogens();

        return reactionBuilder()
            .addReactant(reactant.molecule)
            .addReactant(DestroyMolecules.CYANAMIDE)
            .addProduct(moleculeBuilder().structure(structure).build())
            .build();
    };
    
};
