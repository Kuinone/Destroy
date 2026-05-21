package petrolpark.mc.destroy.core;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

/**
 * Inserts Destroy's Inn structures into vanilla village template pools (desert_inn, plains_inn).
 * Called from a {@code ServerAboutToStartEvent} handler so the pool registries are alive.
*/
public class DestroyVillageAddition {

    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY =
        ResourceKey.create(Registries.PROCESSOR_LIST,
            ResourceLocation.fromNamespaceAndPath("minecraft", "empty"));

    public static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                         Registry<StructureProcessorList> processorListRegistry,
                                         ResourceLocation poolResourceLocation,
                                         String nbtPieceResourceLocation,
                                         int weight) {
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);
        StructureTemplatePool pool = templatePoolRegistry.get(poolResourceLocation);
        if (pool == null) return;
        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceResourceLocation, emptyProcessorList)
            .apply(StructureTemplatePool.Projection.RIGID);
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }
        // Compatibility safety — also append to rawTemplates so other mods iterating both lists see it.
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }
}
