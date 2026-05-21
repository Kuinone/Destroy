package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;

public class HalideCyanideSubstitution extends HalideSubstitution {

    public HalideCyanideSubstitution() {
        super(Destroy.asResource("halide_cyanide_substitution"));
    };

    @Override
    public LegacyMolecularStructure getSubstitutedGroup() {
        return LegacyMolecularStructure.atom(LegacyElement.CARBON)
            .addAtom(LegacyElement.NITROGEN, BondType.TRIPLE);
    };

    @Override
    public LegacySpecies getNucleophile() {
        return DestroyMolecules.CYANIDE;
    };
    
};
