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
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;

/**
 * Reload listener that populates {@link LegacyReaction#REACTIONS} from
 * {@code data/<ns>/destroy/reactions/<name>.json} files. Every {@code /reload}, all previously
 * loaded datapack reactions are cleared (Java-side built-in reactions persist), then the new set
 * is decoded via {@link ReactionDefinition#CODEC} and registered through the same
 * {@link LegacyReaction.ReactionBuilder} call chain the built-in reactions use.
 *
 * <p>JSONs that fail to decode or that reference unknown molecules/items are skipped with a logger
 * warning; the reload as a whole does not fail. This matches the failure mode of vanilla recipes
 * and Vat-material datapacks.</p>
 *
 * <p>This is server-side only. Datapack reactions are visible to client JEI lookups in single
 * player (shared JVM) but in multiplayer the client only sees the built-in reaction list — the
 * actual chemistry simulation still runs server-side and is correct.</p>
 */
public class ReactionDataReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    /** Last-loaded set of datapack reaction definitions, kept around so the per-player
     * {@code OnDatapackSyncEvent} handler can resend them to clients that join after a
     * datapack reload without having to re-read the JSONs from disk.*/
    public static volatile Map<ResourceLocation, ReactionDefinition> LAST_LOADED = Map.of();

    public ReactionDataReloadListener() {
        super(GSON, "destroy/reactions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager,
                          ProfilerFiller profiler) {
        // Clear out previous datapack reactions (built-in Java reactions persist).
        LegacyReaction.clearDatapackReactions();

        Map<ResourceLocation, ReactionDefinition> loaded = new LinkedHashMap<>();
        int ok = 0;
        int skipped = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                ReactionDefinition def = ReactionDefinition.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .getOrThrow(msg -> new JsonParseException("Decode error: " + msg));
                if (def.apply(id)) {
                    loaded.put(id, def);
                    ok++;
                } else {
                    skipped++;
                }
            } catch (Throwable t) {
                Destroy.LOGGER.warn("Failed to load datapack reaction {}: {}", id, t.getMessage());
                skipped++;
            }
        }
        LAST_LOADED = loaded;
        Destroy.LOGGER.info("Loaded {} datapack reaction(s); {} skipped.", ok, skipped);

        // Broadcast to all online clients so multiplayer JEI displays match the new set.
        // Wrapped in try/catch for the initial server-startup case where the player network
        // infrastructure isn't ready yet — same pattern VatMaterialResourceListener uses.
        try {
            CatnipServices.NETWORK.sendToAllClients(new SyncReactionsS2CPacket(loaded));
        } catch (NullPointerException e) {
            // Expected during server startup before player network infrastructure is up.
        }
    }
}
