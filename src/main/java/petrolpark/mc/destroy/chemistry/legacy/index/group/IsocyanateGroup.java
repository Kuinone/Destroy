package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;

public class IsocyanateGroup extends LegacyFunctionalGroup<IsocyanateGroup> {

    public final LegacyAtom nonFunctionalCarbon;
    public final LegacyAtom nitrogen;
    public final LegacyAtom functionalCarbon;
    public final LegacyAtom oxygen;

    public IsocyanateGroup(LegacyAtom nonFunctionalCarbon, LegacyAtom nitrogen, LegacyAtom functionalCarbon, LegacyAtom oxygen) {
        this.nonFunctionalCarbon = nonFunctionalCarbon;
        this.nitrogen = nitrogen;
        this.functionalCarbon = functionalCarbon;
        this.oxygen = oxygen;
    };

    @Override
    public LegacyFunctionalGroupType<? extends IsocyanateGroup> getType() {
        return DestroyGroupTypes.ISOCYANATE;
    };
    
};
