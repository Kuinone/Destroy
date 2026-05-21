package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;

public class NonTertiaryAmineGroup extends LegacyFunctionalGroup<NonTertiaryAmineGroup> {

    public final LegacyAtom carbon;
    public final LegacyAtom nitrogen;
    public final LegacyAtom hydrogen;

    public NonTertiaryAmineGroup(LegacyAtom carbon, LegacyAtom nitrogen, LegacyAtom hydrogen) {
        this.carbon = carbon;
        this.nitrogen = nitrogen;
        this.hydrogen = hydrogen;
    };

    @Override
    public LegacyFunctionalGroupType<NonTertiaryAmineGroup> getType() {
        return DestroyGroupTypes.NON_TERTIARY_AMINE;
    };
    
};
