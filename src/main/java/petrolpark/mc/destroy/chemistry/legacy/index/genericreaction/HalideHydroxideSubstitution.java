package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;

public class HalideHydroxideSubstitution extends HalideSubstitution {

    public HalideHydroxideSubstitution() {
        super(Destroy.asResource("halide_hydroxide_substitution"));
    };

    @Override
    public LegacyMolecularStructure getSubstitutedGroup() {
        return LegacyMolecularStructure.alcohol();
    };

    @Override
    public LegacySpecies getNucleophile() {
        return DestroyMolecules.HYDROXIDE;
    };
    
};
