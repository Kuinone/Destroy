package petrolpark.mc.destroy.client;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.content.oil.OilPonderScenes;
import petrolpark.mc.destroy.content.processing.ProcessingPonderScenes;
import petrolpark.mc.destroy.content.processing.dynamo.DynamoPonderScenes;
import petrolpark.mc.destroy.content.processing.trypolithography.TrypolithographyPonderScenes;
import petrolpark.mc.destroy.core.chemistry.ChemistryPonderScenes;
import petrolpark.mc.destroy.core.pollution.PollutionPonderScenes;

/**
 * Central registrar for Destroy's Ponder scenes. **S158 stub → S159 incremental: 4 dynamo storyboards wired**.
 *
 * <p>Pattern: {@code HELPER.forComponents(DestroyBlocks.XYZ).addStoryBoard(resourcePath,
 * XxxPonderScenes::method[, ...tags])}. Each scene-file port appends a block here.</p>
*/
public class DestroyPonderScenes {

    @SuppressWarnings("unused")
    private static PonderSceneRegistrationHelper<ResourceLocation> helper = null;

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        DestroyPonderScenes.helper = helper;
        // RegistryEntry key function — makes HELPER.forComponents(blockEntry) map to the block ID.
        // Registrate 1.21 ItemProviderEntry is 2-param: ItemProviderEntry<R extends ItemLike, T extends R>.
        // RegistryEntry::getId is inherited from 2-param RegistryEntry<R, T>.
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        // Dynamo — 4 storyboards. dynamoCharging also tagged with Create's
        // KINETIC_APPLIANCES so it appears in the relevant Ponder tag index.
        HELPER.forComponents(DestroyBlocks.DYNAMO)
            .addStoryBoard("processing/dynamo/redstone", DynamoPonderScenes::dynamoRedstone)
            .addStoryBoard("processing/dynamo/charging", DynamoPonderScenes::dynamoCharging, AllCreatePonderTags.KINETIC_APPLIANCES)
            .addStoryBoard("processing/dynamo/electrolysis", DynamoPonderScenes::dynamoElectrolysis)
            .addStoryBoard("processing/dynamo/arc_furnace", DynamoPonderScenes::arcFurnace);

        // Trypolithography — 3 storyboards on CIRCUIT_MASK + KEYPUNCH. Pattern
        // overlay instructions (ShowCircuitPatternPonderInstruction) restored in S161.
        HELPER.forComponents(DestroyItems.CIRCUIT_MASK, DestroyBlocks.KEYPUNCH)
            .addStoryBoard("trypolithography/intro", TrypolithographyPonderScenes::intro)
            .addStoryBoard("trypolithography/rotating", TrypolithographyPonderScenes::rotating)
            .addStoryBoard("trypolithography/flipping", TrypolithographyPonderScenes::flipping);

        // Oil — 3 storyboards covering pumpjack drilling, seismometer/seismograph
        // surveying. pumpjack scene gets AllCreatePonderTags.KINETIC_APPLIANCES. Registered
        // redundantly across all 3 seismology/oil components (Pumpjack + Seismograph +
        HELPER.forComponents(DestroyBlocks.PUMPJACK)
            .addStoryBoard("oil/seismometer", OilPonderScenes::seismometer)
            .addStoryBoard("oil/seismograph", OilPonderScenes::seismograph)
            .addStoryBoard("oil/pumpjack", OilPonderScenes::pumpjack, AllCreatePonderTags.KINETIC_APPLIANCES);
        HELPER.forComponents(DestroyItems.SEISMOGRAPH)
            .addStoryBoard("oil/seismometer", OilPonderScenes::seismometer)
            .addStoryBoard("oil/seismograph", OilPonderScenes::seismograph);
        HELPER.forComponents(DestroyItems.SEISMOMETER)
            .addStoryBoard("oil/seismometer", OilPonderScenes::seismometer)
            .addStoryBoard("oil/seismograph", OilPonderScenes::seismograph);

        // Pollution
        // CATALYTIC_CONVERTER: 1 real scene (catalyticConverter); S164 stubs not bound.
        HELPER.forComponents(DestroyBlocks.CATALYTIC_CONVERTER)
            .addStoryBoard("pollution/catalytic_converter", PollutionPonderScenes::catalyticConverter);

        // POLLUTION_SYMBOL: top-level pollution overview tab with all 12 scenes — 4 real, 8 stubs.
        // When S165+ ports SmogPonderInstruction + Vat subsystem, each stub method becomes real
        // without touching this registration list.
        HELPER.forComponents(DestroyItems.POLLUTION_SYMBOL)
            .addStoryBoard("pollution/tanks", PollutionPonderScenes::pipesAndTanks)
            .addStoryBoard("pollution/basins_and_vats", PollutionPonderScenes::basinsAndVats)
            .addStoryBoard("pollution/smog", PollutionPonderScenes::smog)
            .addStoryBoard("pollution/crop_growth_failure", PollutionPonderScenes::cropGrowthFailure)
            .addStoryBoard("pollution/fishing_failure", PollutionPonderScenes::fishingFailure)
            .addStoryBoard("blank_3x3", PollutionPonderScenes::breedingFailure)
            .addStoryBoard("pollution/villager_price_increase", PollutionPonderScenes::villagerPriceIncrease)
            .addStoryBoard("pollution/cancer", PollutionPonderScenes::cancer)
            .addStoryBoard("pollution/acid_rain", PollutionPonderScenes::acidRain)
            .addStoryBoard("pollution/reduction", PollutionPonderScenes::reduction)
            .addStoryBoard("pollution/lightning", PollutionPonderScenes::lightning)
            .addStoryBoard("pollution/catalytic_converter", PollutionPonderScenes::catalyticConverter);

        // Processing. 13 storyboards across 8 components:
        HELPER.forComponents(DestroyBlocks.AGING_BARREL)
            .addStoryBoard("processing/aging_barrel", ProcessingPonderScenes::agingBarrel);
        HELPER.forComponents(DestroyBlocks.BLOWPIPE)
            .addStoryBoard("processing/blowpipe", ProcessingPonderScenes::blowpipe)
            .addStoryBoard("processing/blowpipe_automation", ProcessingPonderScenes::blowpipeAutomation);
        HELPER.forComponents(DestroyBlocks.BUBBLE_CAP)
            .addStoryBoard("processing/bubble_cap/generic", ProcessingPonderScenes::bubbleCapGeneric)
            .addStoryBoard("processing/bubble_cap/mixtures", ProcessingPonderScenes::bubbleCapMixtures)
            .addStoryBoard("pollution/room_temperature", ChemistryPonderScenes::roomTemperature); // S182
        HELPER.forComponents(DestroyBlocks.CENTRIFUGE)
            .addStoryBoard("processing/centrifuge/generic", ProcessingPonderScenes::centrifugeGeneric)
            .addStoryBoard("processing/centrifuge/mixture", ProcessingPonderScenes::centrifugeMixture);
        HELPER.forComponents(DestroyBlocks.COOLER)
            .addStoryBoard("processing/cooler", ProcessingPonderScenes::cooler)
            .addStoryBoard("vat/temperature", ChemistryPonderScenes::vatTemperature, DestroyPonderTags.CHEMISTRY); // S183
        HELPER.forComponents(DestroyBlocks.EXTRUSION_DIE)
            .addStoryBoard("processing/extrusion_die", ProcessingPonderScenes::extrusionDie);
        HELPER.forComponents(DestroyItems.HYPERACCUMULATING_FERTILIZER)
            .addStoryBoard("processing/phytomining", ProcessingPonderScenes::phytomining);
        HELPER.forComponents(DestroyBlocks.MECHANICAL_SIEVE)
            .addStoryBoard("processing/mechanical_sieve", ProcessingPonderScenes::mechanicalSieve);
        HELPER.forComponents(DestroyBlocks.SIPHON)
            .addStoryBoard("processing/siphon", ProcessingPonderScenes::siphon);
        HELPER.forComponents(DestroyBlocks.TREE_TAP)
            .addStoryBoard("processing/tree_tap", ProcessingPonderScenes::treeTap);

        // Chemistry.
        // BUBBLE_CAP chemistry tab, COLORIMETER, COOLER vat temp tab, POLLUTION_SYMBOL tabs,
        // VAT_CONTROLLER, element blocks via PonderIndex-reflection dispatcher); most are blocked
        // on Vat subsystem. S174 wires 2 of 7+ paths:
        // - AllBlocks.BASIN → reactions (S172, real) with DestroyPonderTags.CHEMISTRY tag
        // - DestroyBlocks.PERIODIC_TABLE → periodicTable (S174, real)
        // Other component wirings added incrementally as their stubs land real bodies.
        HELPER.forComponents(AllBlocks.BASIN)
            .addStoryBoard("reactions", ChemistryPonderScenes::reactions, DestroyPonderTags.CHEMISTRY);
        HELPER.forComponents(DestroyBlocks.PERIODIC_TABLE)
            .addStoryBoard("periodic_table", ChemistryPonderScenes::periodicTable);
        // vat/pressure. S181:
        // vat/items. S182: pollution/room_temperature (shared Ponder schematic with BUBBLE_CAP).
        // Other vat-controller-attached Chemistry scenes (vatConstruction / vatFluids /
        // vatTemperature / vatReading / colorimeter / vatUV) wired incrementally as their stubs
        // land real bodies.
        HELPER.forComponents(DestroyBlocks.VAT_CONTROLLER)
            .addStoryBoard("vat/construction", ChemistryPonderScenes::vatConstruction) // S184
            .addStoryBoard("vat/fluids", ChemistryPonderScenes::vatFluids, AllCreatePonderTags.FLUIDS) // S185
            .addStoryBoard("vat/pressure", ChemistryPonderScenes::vatPressure)
            .addStoryBoard("vat/items", ChemistryPonderScenes::vatItems)
            .addStoryBoard("pollution/room_temperature", ChemistryPonderScenes::roomTemperature)
            .addStoryBoard("vat/temperature", ChemistryPonderScenes::vatTemperature) // S183
            .addStoryBoard("vat/reading", ChemistryPonderScenes::vatReading) // S187
            .addStoryBoard("colorimeter", ChemistryPonderScenes::colorimeter) // S188
            .addStoryBoard("vat/uv", ChemistryPonderScenes::vatUVWithBlackLight); // S186

        // BLACKLIGHT block wires vatUVWithBlackLight (with-blacklight narrative variant).
        HELPER.forComponents(DestroyBlocks.BLACKLIGHT)
            .addStoryBoard("vat/uv", ChemistryPonderScenes::vatUVWithBlackLight);

        // COLORIMETER block wires colorimeter scene (observation + redstone readout).
        HELPER.forComponents(DestroyBlocks.COLORIMETER)
            .addStoryBoard("colorimeter", ChemistryPonderScenes::colorimeter);

        // DestroyMiscPonderScenes partial port (T2c). 2 of 4 storyboards wired real:
        // - reactions on MECHANICAL_MIXER (basic scene, self-contained)
        // vatInteraction stub registered on BLAZE_BURNER (T2a Vat full defer).
        HELPER.forComponents(DestroyBlocks.REDSTONE_PROGRAMMER)
            .addStoryBoard("redstone_programmer", DestroyMiscPonderScenes::redstoneProgrammer);
        HELPER.forComponents(AllBlocks.MECHANICAL_MIXER)
            .addStoryBoard("reactions", DestroyMiscPonderScenes::reactions, DestroyPonderTags.CHEMISTRY);
        HELPER.forComponents(AllBlocks.BLAZE_BURNER)
            .addStoryBoard("vat/interaction", DestroyMiscPonderScenes::vatInteraction, DestroyPonderTags.CHEMISTRY);

        // S202+ 起逐步添加其余 scene registration (ExplosivesPonderScenes / etc.) +
        // 替换 pollution 1 stub + processing 2 stubs + chemistry 11 stubs (need Vat subsystem
        // port first) + dispatcher-wired periodicTable scene on individual element blocks
        // (needs PonderIndex-reflection dispatcher port). DestroyMiscPonderScenes vatInteraction
        // stub → real body awaits T2a Vat full port.
    }

    /**
 * Resource location of the Periodic Table Ponder schematic. Used as the storyboard's
 * "schematic location" identifier so {@link #refreshPeriodicTableBlockScenes} can find
 * + remove old entries before re-attaching freshly.
*/
    private static final net.minecraft.resources.ResourceLocation periodicTableSchematicLocation =
        petrolpark.mc.destroy.Destroy.asResource("periodic_table");

    /**
 * Client-side Ponder scene registry refresh dispatcher for the periodicTable story-board.
 * Iterates {@link petrolpark.mc.destroy.content.product.periodictable.PeriodicTableBlock#ELEMENTS
 * PeriodicTableBlock.ELEMENTS}, removes any pre-existing periodic_table storyboard for each
 * element block (idempotent), and re-attaches the periodicTable scene fresh.
*/
    @SuppressWarnings("deprecation")
    public static void refreshPeriodicTableBlockScenes() {
        if (helper == null) return; // Not yet registered — nothing to refresh.
        petrolpark.mc.destroy.content.product.periodictable.PeriodicTableBlock.ELEMENTS.forEach(entry -> {
            entry.blocks().forEach(block -> {
                net.minecraft.resources.ResourceLocation rl =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(block.asItem());
                // Remove stale periodic_table entries for this block id.
                if (net.createmod.ponder.foundation.PonderIndex.getSceneAccess().doScenesExistForId(rl)) {
                    ((petrolpark.mc.destroy.mixin.accessor.PonderSceneRegistryAccessor)
                        net.createmod.ponder.foundation.PonderIndex.getSceneAccess())
                        .getScenes()
                        .get(rl)
                        .removeIf(sb -> sb.getSchematicLocation().equals(periodicTableSchematicLocation));
                }
                // Re-attach the periodic_table storyboard.
                helper.forComponents(rl).addStoryBoard(periodicTableSchematicLocation, ChemistryPonderScenes::periodicTable);
            });
        });
    }
}
