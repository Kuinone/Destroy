package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;

public class NitroGroup extends LegacyFunctionalGroup<NitroGroup> {

    public final LegacyAtom carbon;
    public final LegacyAtom nitrogen;
    public final LegacyAtom firstOxygen;
    public final LegacyAtom secondOxygen;

    public NitroGroup(LegacyAtom carbon, LegacyAtom nitrogen, LegacyAtom firstOxygen, LegacyAtom secondOxygen) {
        this.carbon = carbon;
        this.nitrogen = nitrogen;
        this.firstOxygen = firstOxygen;
        this.secondOxygen = secondOxygen;
    };

    @Override
    public LegacyFunctionalGroupType<? extends NitroGroup> getType() {
        return DestroyGroupTypes.NITRO;
    };
    
};
