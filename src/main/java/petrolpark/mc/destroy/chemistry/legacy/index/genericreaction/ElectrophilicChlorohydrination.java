package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;

public class ElectrophilicChlorohydrination extends ElectrophilicAddition {

    public ElectrophilicChlorohydrination(boolean alkyne) {
        super(Destroy.MOD_ID, "chlorohydrination", alkyne);
    };

    @Override
    public LegacyMolecularStructure getLowDegreeGroup() {
        return LegacyMolecularStructure.atom(LegacyElement.CHLORINE);
    };

    @Override
    public LegacyMolecularStructure getHighDegreeGroup() {
        return LegacyMolecularStructure.alcohol();
    };

    @Override
    public LegacySpecies getElectrophile() {
        return DestroyMolecules.HYPOCHLOROUS_ACID;
    };
    
};
