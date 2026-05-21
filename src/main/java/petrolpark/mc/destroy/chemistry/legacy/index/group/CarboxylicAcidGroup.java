package petrolpark.mc.destroy.chemistry.legacy.index.group;

import petrolpark.mc.destroy.chemistry.legacy.LegacyAtom;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;

public class CarboxylicAcidGroup extends LegacyFunctionalGroup<CarboxylicAcidGroup> {
    public final LegacyAtom carbon;
    public final LegacyAtom carbonylOxygen;
    public final LegacyAtom alcoholOxygen;
    public final LegacyAtom proton;

    public CarboxylicAcidGroup(LegacyAtom carbon, LegacyAtom carbonylOxygen, LegacyAtom alcoholOxygen, LegacyAtom proton) {
        super();
        this.carbon = carbon;
        this.carbonylOxygen = carbonylOxygen;
        this.alcoholOxygen = alcoholOxygen;
        this.proton = proton;
    };

    @Override
    public LegacyFunctionalGroupType<CarboxylicAcidGroup> getType() {
        return DestroyGroupTypes.CARBOXYLIC_ACID;
    };
};
