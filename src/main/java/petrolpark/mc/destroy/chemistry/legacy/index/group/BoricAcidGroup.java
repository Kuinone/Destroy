package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;

public class BoricAcidGroup extends LegacyFunctionalGroup<BoricAcidGroup> {

    public final LegacyAtom boron;
    public final LegacyAtom oxygen;
    public final LegacyAtom hydrogen;

    public BoricAcidGroup(LegacyAtom boron, LegacyAtom oxygen, LegacyAtom hydrogen) {
        this.boron = boron;
        this.oxygen = oxygen;
        this.hydrogen = hydrogen;
    };

    @Override
    public LegacyFunctionalGroupType<? extends BoricAcidGroup> getType() {
        return DestroyGroupTypes.BORIC_ACID;
    };
    
};
