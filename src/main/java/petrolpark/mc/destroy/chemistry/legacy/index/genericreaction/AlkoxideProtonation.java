package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReactant;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.SingleGroupGenericReaction;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.AlkoxideGroup;

public class AlkoxideProtonation extends SingleGroupGenericReaction<AlkoxideGroup> {

    public AlkoxideProtonation() {
        super(Destroy.asResource("alkoxide_protonation"), DestroyGroupTypes.ALKOXIDE);
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<AlkoxideGroup> reactant) {
        LegacyMolecularStructure structure = reactant.getMolecule().shallowCopyStructure();
        structure.moveTo(reactant.group.carbon)
            .remove(reactant.group.oxygen)
            .addGroup(LegacyMolecularStructure.alcohol());
        return reactionBuilder()
            .addReactant(reactant.molecule)
            .addReactant(DestroyMolecules.PROTON)
            .addProduct(moleculeBuilder().structure(structure).build())
            .build();
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.PROTON) > 0f;
    };
    
};
