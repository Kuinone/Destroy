package petrolpark.mc.destroy;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Destroy's custom {@link VillagerProfession} + {@link PoiType} registry. Currently defines one
 * profession ({@link #INNKEEPER}) and its workstation POI ({@link #AGING_BARREL_POI}) — Innkeepers
 * work at Aging Barrels instead of standard vanilla POI blocks. Used by the
 * {@link petrolpark.mc.destroy.content.processing.ProcessingPonderScenes#agingBarrel} Ponder scene
 * + runtime villager-profession assignment.
 *
 * <p>Registered via {@link #register(IEventBus)} from {@link Destroy#Destroy}.</p>
*/
public class DestroyVillagers {

    private static final DeferredRegister<PoiType> POI_TYPES =
        DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Destroy.MOD_ID);
    private static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
        DeferredRegister.create(Registries.VILLAGER_PROFESSION, Destroy.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> AGING_BARREL_POI =
        POI_TYPES.register("aging_barrel_poi", () -> new PoiType(
            ImmutableSet.copyOf(DestroyBlocks.AGING_BARREL.get().getStateDefinition().getPossibleStates()),
            1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> INNKEEPER =
        VILLAGER_PROFESSIONS.register("innkeeper", () -> new VillagerProfession(
            "innkeeper",
            poi -> poi.value() == AGING_BARREL_POI.get(),
            poi -> poi.value() == AGING_BARREL_POI.get(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_SHEPHERD));

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }
}
