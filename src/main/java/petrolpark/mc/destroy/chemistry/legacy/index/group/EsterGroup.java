package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;

public class EsterGroup extends LegacyFunctionalGroup<EsterGroup> {

    private LegacyAtom carbonylCarbon;
    private LegacyAtom alcoholCarbon;
    private LegacyAtom carbonylOxygen;
    private LegacyAtom bridgeOxygen;

    public EsterGroup() {
        super();
    };

    public EsterGroup(LegacyAtom carbonylCarbon, LegacyAtom alcoholCarbon, LegacyAtom carbonylOxygen, LegacyAtom bridgeOxygen) {
        super();
        this.carbonylCarbon = carbonylCarbon;
        this.alcoholCarbon = alcoholCarbon;
        this.carbonylOxygen = carbonylOxygen;
        this.bridgeOxygen = bridgeOxygen;
    };

    public LegacyAtom getCarbonylCarbon() {
        return carbonylCarbon;
    };

    public LegacyAtom getAlcoholCarbon() {
        return alcoholCarbon;
    };

    public LegacyAtom getCarbonylOxygen() {
        return carbonylOxygen;
    };

    public LegacyAtom getBridgeOxygen() {
        return bridgeOxygen;
    };

    @Override
    public LegacyFunctionalGroupType<EsterGroup> getType() {
        return DestroyGroupTypes.ESTER;
    };
    
};
