package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.CarbonylGroup;

public class CyanideNucleophilicAddition extends SingleGroupGenericReaction<CarbonylGroup> {

    public CyanideNucleophilicAddition() {
        super(Destroy.asResource("cyanide_nucleophilic_addition"), DestroyGroupTypes.CARBONYL);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.CYANIDE) > 0f && mixture.getConcentrationOf(DestroyMolecules.PROTON) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<CarbonylGroup> reactant) {
        LegacySpecies reactantMolecule = reactant.getMolecule();
        CarbonylGroup carbonyl = reactant.getGroup();
        LegacySpecies productMolecule = moleculeBuilder().structure(reactantMolecule.shallowCopyStructure()
            .moveTo(carbonyl.carbon)
            .remove(carbonyl.oxygen)
            .addGroup(LegacyMolecularStructure.alcohol())
            .addGroup(LegacyMolecularStructure.atom(LegacyElement.CARBON).addAtom(LegacyElement.NITROGEN, BondType.TRIPLE))
        ).build();
        return reactionBuilder()
            .addReactant(reactantMolecule)
            .addReactant(DestroyMolecules.HYDROGEN_CYANIDE)
            .addCatalyst(DestroyMolecules.CYANIDE, 1)
            .addProduct(productMolecule)
            // TODO rate constants
            .build();
    };
    
};
