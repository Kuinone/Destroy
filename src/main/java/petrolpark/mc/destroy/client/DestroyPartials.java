package petrolpark.mc.destroy.client;

import java.util.ArrayList;
import java.util.List;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.lang.Lang;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyBond.BondType;
import petrolpark.mc.destroy.chemistry.legacy.LegacyElement;

/**
 * Destroy's {@link PartialModel} registry — all client-only ball-and-stick + machine-internal
 * sub-models loaded at resource-reload and handed to block-entity / item / molecule renderers.
*/
@OnlyIn(Dist.CLIENT)
public class DestroyPartials {

    public static final PartialModel

    AIR = PartialModel.of(ResourceLocation.fromNamespaceAndPath("minecraft", "block/air")),

    // Kinetics
    CENTRIFUGE_COG = block("centrifuge/inner"),
    DYNAMO_SHAFT = block("dynamo/inner"),
    ARC_FURNACE_SHAFT = block("dynamo/arc_furnace_inner"),

    // Pumpjack
    PUMPJACK_CAM = block("pumpjack/cam"),
    PUMPJACK_LINKAGE = block("pumpjack/linkage"),
    PUMPJACK_BEAM = block("pumpjack/beam"),
    PUMPJACK_PUMP = block("pumpjack/pump"),

    // Pollutometer
    POLLUTOMETER_ANEMOMETER = block("pollutometer/anemometer"),
    POLLUTOMETER_WEATHERVANE = block("pollutometer/weathervane"),

    // Vat
    VAT_SIDE_PIPE = block("vat_side/pipe"),
    VAT_SIDE_BAROMETER = block("vat_side/barometer"),
    VAT_SIDE_BAROMETER_DIAL = block("vat_side/barometer_dial"),
    VAT_SIDE_REDSTONE_INTERFACE = block("vat_side/redstone_interface"),
    VAT_SIDE_THERMOMETER = block("vat_side/thermometer"),
    VAT_SIDE_VENT = block("vat_side/vent"),
    VAT_SIDE_VENT_BAR = block("vat_side/vent_bar"),

    // Redstone Programmer
    REDSTONE_PROGRAMMER_CYLINDER = block("redstone_programmer/cylinder"),
    REDSTONE_PROGRAMMER_NEEDLE = block("redstone_programmer/needle"),
    REDSTONE_PROGRAMMER_TRANSMITTER = block("redstone_programmer/transmitter"),
    REDSTONE_PROGRAMMER_TRANSMITTER_POWERED = block("redstone_programmer/transmitter_powered"),

    // Miscellaneous
    TREE_TAP_ARM = block("tree_tap/arm"),
    KEYPUNCH_PISTON = block("keypunch/piston"),
    LABORATORY_GOGGLES = block("laboratory_goggles"),
    GOLD_LABORATORY_GOGGLES = block("gold_laboratory_goggles"),
    GAS_MASK = block("gas_mask"),
    PAPER_MASK = block("paper_mask"),
    STRAY_SKULL = block("cooler/skull"),

    // Explosive stuff
    CUSTOM_EXPLOSIVE_MIX_BASE = block("custom_explosive_mix_no_overlay"),
    CUSTOM_EXPLOSIVE_MIX_OVERLAY = block("custom_explosive_mix_overlay"),
    CUSTOM_EXPLOSIVE_MIX_SHELL_BASE = block("custom_explosive_mix_shell_no_overlay"),
    CUSTOM_EXPLOSIVE_MIX_SHELL_OVERLAY = block("custom_explosive_mix_shell_overlay"),

    // Mechanical Sieve
    MECHANICAL_SIEVE_SHAFT = block("mechanical_sieve/shaft"),
    MECHANICAL_SIEVE_LINKAGES = block("mechanical_sieve/linkages"),
    MECHANICAL_SIEVE = block("mechanical_sieve/sieve");

    // Atoms: per-element ball models attached directly to the LegacyElement enum so the
    // MoleculeRenderer can look up an element's visual by enum constant without a side table.
    static {
        for (LegacyElement element : LegacyElement.values()) {
            if (element != LegacyElement.R_GROUP) element.setPartial(atom(Lang.asId(element.name())));
        }
    }

    // Bonds: same pattern for bond types (SINGLE/DOUBLE/TRIPLE/AROMATIC).
    static {
        for (BondType bondType : BondType.values()) {
            bondType.setPartial(bond(Lang.asId(bondType.name())));
        }
    }

    // R-Groups: generic + 1..9 numbered variants for labelling R₁/R₂/... in multi-R generic reactions.
    public static final PartialModel R_GROUP = rGroup("generic");
    public static final List<PartialModel> rGroups = new ArrayList<>(10);
    static {
        rGroups.add(R_GROUP);
        for (int i = 1; i < 10; i++) {
            rGroups.add(rGroup(String.valueOf(i)));
        }
    }

    private static PartialModel block(String path) {
        return PartialModel.of(Destroy.asResource("block/" + path));
    }

    private static PartialModel atom(String path) {
        return PartialModel.of(Destroy.asResource("chemistry/atom/" + path));
    }

    private static PartialModel bond(String path) {
        return PartialModel.of(Destroy.asResource("chemistry/bond/" + path));
    }

    private static PartialModel rGroup(String path) {
        return PartialModel.of(Destroy.asResource("chemistry/r_group/" + path));
    }

    /**
 * Class-load trigger for the static {@link PartialModel} constants + per-element/per-bond
 * {@code setPartial} initializers. Call once from client mod setup.
*/
    public static void init() {}
}
