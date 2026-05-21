package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import java.util.List;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction.ReactionBuilder;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.AlcoholGroup;

public class AlcoholOxidation extends SingleGroupGenericReaction<AlcoholGroup> {

    public AlcoholOxidation() {
        super(Destroy.asResource("alcohol_oxidation"), DestroyGroupTypes.ALCOHOL);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.DICHROMATE) > 0f && mixture.getConcentrationOf(DestroyMolecules.PROTON) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<AlcoholGroup> reactant) {
        AlcoholGroup alcohol = reactant.getGroup();
        if (alcohol.degree >= 3) return null;
        LegacyMolecularStructure structure = reactant.getMolecule().shallowCopyStructure();
        List<LegacyAtom> hydrogens = structure.moveTo(alcohol.carbon).getBondedAtomsOfElement(LegacyElement.HYDROGEN);
        if (hydrogens.isEmpty()) return null; // This should never be the case
        structure
            .remove(hydrogens.get(0))
            .moveTo(alcohol.oxygen)
            .remove(alcohol.hydrogen)
            .replaceBondTo(alcohol.carbon, BondType.DOUBLE);
        LegacySpecies product = moleculeBuilder().structure(structure).build();
        ReactionBuilder builder = reactionBuilder()
            .addReactant(reactant.getMolecule(), 3, 1)
            .addReactant(DestroyMolecules.DICHROMATE)
            .addReactant(DestroyMolecules.PROTON, 8, 1)
            .addProduct(product, 3)
            .addProduct(DestroyMolecules.CHROMIUM_III, 2)
            .addProduct(DestroyMolecules.WATER, 7)
            .activationEnergy(25f);
        if (product == DestroyMolecules.ACETONE) builder.withResult(0f, DestroyAdvancementTrigger.ACETONE::asReactionResult);
        
        return builder.build();
    };
    
    @Override
    public LegacyReaction generateExampleReaction() {
        LegacyAtom hydrogen = new LegacyAtom(LegacyElement.HYDROGEN);
        LegacyAtom oxygen = new LegacyAtom(LegacyElement.OXYGEN);
        LegacyAtom carbon = new LegacyAtom(LegacyElement.CARBON);
        LegacyAtom rGroup1 = new LegacyAtom(LegacyElement.R_GROUP);
        LegacyAtom rGroup2 = new LegacyAtom(LegacyElement.R_GROUP);
        rGroup1.rGroupNumber = 1;
        rGroup2.rGroupNumber = 2;
        LegacySpecies exampleMolecule = moleculeBuilder()
            .structure(
                new LegacyMolecularStructure(carbon)
                .addAtom(rGroup1)
                .addAtom(rGroup2)
                .addAtom(LegacyElement.HYDROGEN)
                .addGroup(new LegacyMolecularStructure(oxygen).addAtom(hydrogen))
            ).build();
        return generateReaction(new GenericReactant<>(exampleMolecule, new AlcoholGroup(carbon, oxygen, hydrogen, 2)));
    };
};
