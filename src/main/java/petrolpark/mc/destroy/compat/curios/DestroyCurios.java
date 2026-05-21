package petrolpark.mc.destroy.compat.curios;

import com.petrolpark.compat.curios.PetrolparkCurios;

import net.neoforged.bus.api.IEventBus;
import petrolpark.mc.destroy.core.chemistry.hazard.ChemistryHazardHelper.Protection;

/**
 * Destroy's Curios compatibility wiring.
 *
 * <p>Loaded without guarantee that Curios is installed — the call site in
 * {@code Destroy} is gated behind {@code Mods.CURIOS.executeIfInstalled(...)}.</p>
 *
 * <p>1.21.1 notes:</p>
 * <ul>
 * <li>Library API renamed: {@code com.petrolpark.compat.curios.Curios} →
 * {@link PetrolparkCurios}, {@code CuriosSetup} →
 * {@link com.petrolpark.compat.curios.PetrolparkCuriosSetup}.</li>
 * <li>The library's {@code PetrolparkCurios#ctor} already attaches the
 * {@code ENGINEERS_GOGGLES} predicate to Create's {@code GogglesItem}, so Destroy no longer
 * needs to call {@code GogglesItem.addIsWearingPredicate(...)} here — doing so would double-
 * register the same predicate.</li>
 * <li>Slot opt-in has moved from {@code ICurioItem} capability attachment to the
 * {@code curios:tag} validator + item tag {@code data/curios/tags/item/<slot>.json}. Items
 * are tagged in {@code DestroyItems} via their Curios Registrate transforms — no runtime
 * registration needed here.</li>
 * </ul>
*/
public class DestroyCurios {

    public static void init(IEventBus modEventBus, IEventBus forgeEventBus) {
        // Protective equipment → matching body-part protection predicate (all go in the "head" slot
        // except nothing currently — BODY/LEGS/FEET are covered by vanilla armor slots, which the
        // Protection enum's default test already handles).
        registerCuriosTest(Protection.HEAD,          "head");
        registerCuriosTest(Protection.EYES,          "head");
        registerCuriosTest(Protection.NOSE,          "head");
        registerCuriosTest(Protection.MOUTH,         "head");
        registerCuriosTest(Protection.MOUTH_COVERED, "head");
    }

    private static void registerCuriosTest(Protection protectionType, String slotId) {
        protectionType.registerTest(PetrolparkCurios.wearingCurioPredicate(protectionType.defaultTag::matches, slotId));
    }
}
