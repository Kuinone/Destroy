package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction.ReactionBuilder;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;

public class SaturatedCarbonHydrolysis extends ElectrophilicAddition {

    public SaturatedCarbonHydrolysis(boolean alkyne) {
        super(Destroy.MOD_ID, "hydrolysis", alkyne);
    };

    @Override
    public LegacyMolecularStructure getLowDegreeGroup() {
        return LegacyMolecularStructure.atom(LegacyElement.HYDROGEN);
    };

    @Override
    public LegacyMolecularStructure getHighDegreeGroup() {
        return LegacyMolecularStructure.alcohol();
    };

    @Override
    public LegacySpecies getElectrophile() {
        return DestroyMolecules.WATER;
    };

    @Override
    public void transform(ReactionBuilder builder) {
        builder
            .displayAsReversible()
            .addCatalyst(DestroyMolecules.PROTON, 2)
            .activationEnergy(20f);
        if (builder.hasReactant(DestroyMolecules.PROPENE)) builder.withResult(0f, DestroyAdvancementTrigger.PROPANOL::asReactionResult);
    };

    
};
