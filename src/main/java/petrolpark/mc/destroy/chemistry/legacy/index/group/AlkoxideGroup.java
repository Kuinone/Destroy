package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;

public class AlkoxideGroup extends LegacyFunctionalGroup<AlkoxideGroup> {

    public final LegacyAtom carbon;
    public final LegacyAtom oxygen;

    public AlkoxideGroup(LegacyAtom carbon, LegacyAtom oxygen) {
        this.carbon = carbon;
        this.oxygen = oxygen;
    };

    @Override
    public LegacyFunctionalGroupType<? extends AlkoxideGroup> getType() {
        return DestroyGroupTypes.ALKOXIDE;
    };
    
};
