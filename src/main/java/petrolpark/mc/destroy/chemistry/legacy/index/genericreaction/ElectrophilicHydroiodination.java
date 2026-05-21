package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;

public class ElectrophilicHydroiodination extends ElectrophilicAddition {

    public ElectrophilicHydroiodination(boolean alkyne) {
        super(Destroy.MOD_ID, "hydroiodination", alkyne);
    };

    @Override
    public LegacyMolecularStructure getLowDegreeGroup() {
        return LegacyMolecularStructure.atom(LegacyElement.HYDROGEN);
    };

    @Override
    public LegacyMolecularStructure getHighDegreeGroup() {
        return LegacyMolecularStructure.atom(LegacyElement.IODINE);
    };

    @Override
    public LegacySpecies getElectrophile() {
        return DestroyMolecules.HYDROGEN_IODIDE;
    };
    
};
