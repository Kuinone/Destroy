package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;

public class AlkeneGroup extends SaturatedCarbonGroup {

    public AlkeneGroup(LegacyAtom highDegreeCarbon, LegacyAtom lowDegreeCarbon) {
        super(highDegreeCarbon, lowDegreeCarbon);
    };

    @Override
    public LegacyFunctionalGroupType<? extends SaturatedCarbonGroup> getType() {
        return DestroyGroupTypes.ALKENE;
    };
    
}
