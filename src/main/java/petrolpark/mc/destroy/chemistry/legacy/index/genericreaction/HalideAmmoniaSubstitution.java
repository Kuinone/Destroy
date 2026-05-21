package petrolpark.mc.destroy.chemistry.legacy.index.genericreaction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction.ReactionBuilder;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.chemistry.legacy.index.group.HalideGroup;

public class HalideAmmoniaSubstitution extends HalideSubstitution {

    public HalideAmmoniaSubstitution() {
        super(Destroy.asResource("halide_ammonia_substitution"));
    };

    @Override
    public boolean isPossibleIn(ReadOnlyMixture mixture) {
        return mixture.getConcentrationOf(DestroyMolecules.AMMONIA) > 0f;
    }

    @Override
    public LegacyMolecularStructure getSubstitutedGroup() {
        return LegacyMolecularStructure.atom(LegacyElement.NITROGEN)
            .addAtom(LegacyElement.HYDROGEN)
            .addAtom(LegacyElement.HYDROGEN);
    };

    @Override
    public LegacySpecies getNucleophile() {
        return null;
    };

    @Override
    public void transform(ReactionBuilder builder, HalideGroup group) {
        builder.addReactant(DestroyMolecules.AMMONIA, 2, group.degree == 3 ? 1 : 2)
            .addProduct(DestroyMolecules.AMMONIUM);
    };
    
};
