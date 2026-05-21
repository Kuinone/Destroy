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
import petrolpark.mc.destroy.chemistry.legacy.index.group.NitrileGroup;

public class NitrileHydrolysis extends SingleGroupGenericReaction<NitrileGroup> {

    public NitrileHydrolysis() {
        super(Destroy.asResource("nitrile_hydrolysis"), DestroyGroupTypes.NITRILE);
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.WATER) > 0f;
    };

    @Override
    public LegacyReaction generateReaction(GenericReactant<NitrileGroup> reactant) {
        NitrileGroup group = reactant.getGroup();
        LegacyMolecularStructure structure = reactant.getMolecule().shallowCopyStructure();
        structure.moveTo(group.carbon)
                .remove(group.nitrogen)
                .addCarbonyl()
                .addGroup(
                        LegacyMolecularStructure.atom(LegacyElement.NITROGEN)
                                .addAtom(LegacyElement.HYDROGEN)
                                .addAtom(LegacyElement.HYDROGEN)
                );
        return reactionBuilder()
                .addReactant(reactant.getMolecule())
                .addReactant(DestroyMolecules.WATER)
                .addCatalyst(DestroyMolecules.PROTON, 1)
                .addProduct(moleculeBuilder().structure(structure).build())
                .build();
    };

};