package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;

public class HalideGroup extends LegacyFunctionalGroup<HalideGroup> {

    public final LegacyAtom carbon;
    public final  LegacyAtom halogen;
    public int degree;

    public HalideGroup(LegacyAtom carbon, LegacyAtom halogen, int degree) {
        this.carbon = carbon;
        this.halogen = halogen;
        this.degree = degree;
    };

    @Override
    public LegacyFunctionalGroupType<HalideGroup> getType() {
        return DestroyGroupTypes.HALIDE;
    };
    
};
