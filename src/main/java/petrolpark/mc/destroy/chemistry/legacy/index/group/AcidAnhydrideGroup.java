package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;

public class AcidAnhydrideGroup extends LegacyFunctionalGroup<AcidAnhydrideGroup> {

    private LegacyAtom firstCarbon;
    private LegacyAtom firstOxygen;
    private LegacyAtom secondCarbon;
    private LegacyAtom secondOxygen;
    private LegacyAtom bridgingOxygen;

    public AcidAnhydrideGroup() {
        super();
    };

    public AcidAnhydrideGroup(LegacyAtom firstCarbon, LegacyAtom firstOxygen, LegacyAtom secondCarbon, LegacyAtom secondOxygen, LegacyAtom bridgingOxygen) {
        super();
        this.firstCarbon = firstCarbon;
        this.firstOxygen = firstOxygen;
        this.secondCarbon = secondCarbon;
        this.secondOxygen = secondOxygen;
        this.bridgingOxygen = bridgingOxygen;
    };

    public LegacyAtom getFirstCarbon() {
        return firstCarbon;
    };

    public LegacyAtom getFirstOxygen() {
        return firstOxygen;
    };

    public LegacyAtom getSecondCarbon() {
        return secondCarbon;
    };

    public LegacyAtom getSecondOxygen() {
        return secondOxygen;
    };

    public LegacyAtom getBridgingOxygen() {
        return bridgingOxygen;
    };

    @Override
    public LegacyFunctionalGroupType<AcidAnhydrideGroup> getType() {
        return DestroyGroupTypes.ACID_ANHYDRIDE;
    };
    
};
