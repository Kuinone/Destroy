package petrolpark.mc.destroy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DestroyStats {

    public static final DeferredRegister<ResourceLocation> STATS = DeferredRegister.create(Registries.CUSTOM_STAT, Destroy.MOD_ID);

    private static final Map<ResourceLocation, StatFormatter> FORMATTERS = new HashMap<>();

    public static final DeferredHolder<ResourceLocation, ResourceLocation> NOVEL_COMPOUNDS_SYNTHESIZED = register("novel_compounds_synthesized", StatFormatter.DEFAULT);

    private static DeferredHolder<ResourceLocation, ResourceLocation> register(String name, StatFormatter formatter) {
        ResourceLocation rl = Destroy.asResource(name);
        DeferredHolder<ResourceLocation, ResourceLocation> stat = STATS.register(name, () -> rl);
        FORMATTERS.put(rl, formatter);
        return stat;
    }

    public static void register(IEventBus modEventBus) {
        STATS.register(modEventBus);
    }

    // Called at FMLCommonSetupEvent (see Destroy#init) after STATS are registered to their vanilla registry.
    public static void registerFormatters() {
        for (Entry<ResourceLocation, StatFormatter> entry : FORMATTERS.entrySet()) {
            Stats.CUSTOM.get(entry.getKey(), entry.getValue());
        }
    }
}
