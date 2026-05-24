package petrolpark.mc.destroy.core.chemistry.data;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

/**
 * Reload listener that populates {@link LegacySpecies#MOLECULES} from
 * {@code data/<ns>/destroy/molecules/<name>.json} files.
 *
 * <p>MUST be registered BEFORE {@link ReactionDataReloadListener} (in {@code DestroyCommonEvents})
 * because datapack reactions may reference datapack molecules — molecules must exist in
 * {@code MOLECULES} when {@code ReactionDefinition.apply()} calls {@code LegacySpecies.getMolecule()}.</p>
 *
 * <p>Failures are non-fatal per-entry: a JSON that can't be decoded (or that references an
 * unparseable FROWNS string) is logged and skipped, the rest of the reload continues.</p>
 */
public class MoleculeDataReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    /** Last-loaded set of datapack molecule definitions, kept for the per-player resync handler.*/
    public static volatile Map<ResourceLocation, MoleculeDefinition> LAST_LOADED = Map.of();

    public MoleculeDataReloadListener() {
        super(GSON, "destroy/molecules");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager,
                          ProfilerFiller profiler) {
        LegacySpecies.clearDatapackMolecules();

        Map<ResourceLocation, MoleculeDefinition> loaded = new LinkedHashMap<>();
        int ok = 0;
        int skipped = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                MoleculeDefinition def = MoleculeDefinition.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .getOrThrow(msg -> new JsonParseException("Decode error: " + msg));
                if (def.apply(id)) {
                    loaded.put(id, def);
                    ok++;
                } else {
                    skipped++;
                }
            } catch (Throwable t) {
                Destroy.LOGGER.warn("Failed to load datapack molecule {}: {}", id, t.getMessage());
                skipped++;
            }
        }
        LAST_LOADED = loaded;
        Destroy.LOGGER.info("Loaded {} datapack molecule(s); {} skipped.", ok, skipped);

        try {
            CatnipServices.NETWORK.sendToAllClients(new SyncMoleculesS2CPacket(loaded));
        } catch (NullPointerException e) {
            // Expected during server startup before player network infrastructure is up.
        }
    }
}
