package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.PrimaryAmineGroup;

public class AminePhosgenation extends SingleGroupGenericReaction<PrimaryAmineGroup> {

    public AminePhosgenation() {
        super(Destroy.asResource("amine_phosgenation"), DestroyGroupTypes.PRIMARY_AMINE);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.PHOSGENE) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<PrimaryAmineGroup> reactant) {
        PrimaryAmineGroup group = reactant.group;
        LegacyMolecularStructure structure = reactant.molecule.shallowCopyStructure();

        LegacyAtom carbon = new LegacyAtom(LegacyElement.CARBON);
        structure.moveTo(group.nitrogen)
            .remove(group.firstHydrogen)
            .remove(group.secondHydrogen)
            .addAtom(carbon, BondType.DOUBLE)
            .moveTo(carbon)
            .addAtom(LegacyElement.OXYGEN, BondType.DOUBLE);
        return reactionBuilder()
            .addReactant(reactant.molecule)
            .addReactant(DestroyMolecules.PHOSGENE)
            .addProduct(DestroyMolecules.HYDROCHLORIC_ACID, 2)
            .addProduct(moleculeBuilder().structure(structure).build())
            .build();

    };
    
};
