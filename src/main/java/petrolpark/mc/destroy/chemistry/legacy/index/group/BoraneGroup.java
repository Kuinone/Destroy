package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;

public class BoraneGroup extends LegacyFunctionalGroup<BoraneGroup> {

    public final LegacyAtom carbon;
    public final LegacyAtom boron;

    public BoraneGroup(LegacyAtom carbon, LegacyAtom boron) {
        this.carbon = carbon;
        this.boron = boron;
    };

    @Override
    public LegacyFunctionalGroupType<? extends BoraneGroup> getType() {
        return DestroyGroupTypes.BORANE;
    };
    
};
