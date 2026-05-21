package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.CarbonylGroup;

public class BorohydrideCarbonylReduction extends SingleGroupGenericReaction<CarbonylGroup> {

    public BorohydrideCarbonylReduction() {
        super(Destroy.asResource("borohydride_carbonyl_reduction"), DestroyGroupTypes.CARBONYL);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.BOROHYDRIDE) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<CarbonylGroup> reactant) {
        CarbonylGroup carbonyl = reactant.getGroup();

        LegacyMolecularStructure alkoxideStructure = reactant.getMolecule().shallowCopyStructure();
        alkoxideStructure.moveTo(carbonyl.carbon)
            .addAtom(LegacyElement.HYDROGEN)
            .replaceBondTo(carbonyl.oxygen, BondType.SINGLE)
            .remove(carbonyl.oxygen)
            .addAtom(new LegacyAtom(LegacyElement.OXYGEN, -1d));

        LegacyMolecularStructure borateEsterStructure = reactant.getMolecule().shallowCopyStructure();
        borateEsterStructure.moveTo(carbonyl.carbon)
            .addAtom(LegacyElement.HYDROGEN)
            .replaceBondTo(carbonyl.oxygen, BondType.SINGLE)
            .moveTo(carbonyl.oxygen)
            .addGroup(LegacyMolecularStructure.borane());

        return reactionBuilder()
            .addReactant(reactant.getMolecule(), 2)
            .addReactant(DestroyMolecules.BOROHYDRIDE)
            .addProduct(moleculeBuilder().structure(alkoxideStructure).build())
            .addProduct(moleculeBuilder().structure(borateEsterStructure).build())
            .activationEnergy(50f)
            .build();
    };
    
};
