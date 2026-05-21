package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;

public class NitrileGroup extends LegacyFunctionalGroup<NitrileGroup> {

    public final LegacyAtom carbon;
    public final LegacyAtom nitrogen;

    public NitrileGroup(LegacyAtom carbon, LegacyAtom nitrogen) {
        this.carbon = carbon;
        this.nitrogen = nitrogen;
    };

    @Override
    public LegacyFunctionalGroupType<NitrileGroup> getType() {
        return DestroyGroupTypes.NITRILE;
    };
    
};
