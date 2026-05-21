package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;

public class AlcoholGroup extends LegacyFunctionalGroup<AlcoholGroup> {

    public final LegacyAtom carbon;
    public final LegacyAtom oxygen;
    public final LegacyAtom hydrogen;

    public final int degree;

    public AlcoholGroup(LegacyAtom carbon, LegacyAtom oxygen, LegacyAtom hydrogen, int degree) {
        super();
        this.carbon = carbon;
        this.oxygen = oxygen;
        this.hydrogen = hydrogen;
        this.degree = degree;
    };

    @Override
    public LegacyFunctionalGroupType<AlcoholGroup> getType() {
        return DestroyGroupTypes.ALCOHOL;
    };
    
};
