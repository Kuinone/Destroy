package petrolpark.mc.destroy.content.processing.trypolithography;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

import net.createmod.catnip.levelWrappers.WorldHelper;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.world.level.LevelAccessor;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.content.processing.trypolithography.keypunch.ICircuitPuncher;

/**
 * Per-level registry of {@link ICircuitPuncher}s (one UUID → one keypunch block entity). When a
 * {@code CircuitMaskItem} is punched, the mask stores the keypunch's UUID in its {@code PUNCHED_BY}
 * DataComponent list; later, on mask hover tooltip, the UUID is resolved back to a puncher name
 * via {@link #getPuncher}. Keypunches register themselves on BE load and remove themselves on BE
 * unload / block break.
 *
 * <p>Singleton access: {@link Destroy#CIRCUIT_PUNCHER_HANDLER}.</p>
*/
public class CircuitPuncherHandler {

    private static final Map<LevelAccessor, Map<UUID, ICircuitPuncher>> connections = new IdentityHashMap<>();

    /** Sentinel returned when a UUID isn't registered (e.g., keypunch destroyed or world not loaded yet).*/
    public static final ICircuitPuncher UNKNOWN = new ICircuitPuncher() {

        private static final UUID uuid = UUID.fromString("63fec9a5-94f3-4b5d-b600-433c8c779400");

        @Override
        public UUID getUUID() {
            return uuid;
        }

        @Override
        public String getName() {
            return DestroyLang.translate("tooltip.circuit_mask.unknown_circuit_puncher").string();
        }
    };

    public ICircuitPuncher getPuncher(LevelAccessor world, UUID uuid) {
        Map<UUID, ICircuitPuncher> punchersInWorld = punchersIn(world);
        ICircuitPuncher puncher = punchersInWorld.get(uuid);
        if (puncher == null) return UNKNOWN;
        return puncher;
    }

    public void onLoadWorld(LevelAccessor world) {
        connections.put(world, new HashMap<>());
        Destroy.LOGGER.debug("Prepared circuit puncher handler for " + WorldHelper.getDimensionID(world));
    }

    public void onUnloadWorld(LevelAccessor world) {
        connections.remove(world);
        Destroy.LOGGER.debug("Removed circuit puncher handler for " + WorldHelper.getDimensionID(world));
    }

    public void addPuncher(LevelAccessor world, ICircuitPuncher puncher) {
        Map<UUID, ICircuitPuncher> punchersInWorld = punchersIn(world);
        punchersInWorld.put(puncher.getUUID(), puncher);
    }

    public void removePuncher(LevelAccessor world, ICircuitPuncher puncher) {
        Map<UUID, ICircuitPuncher> punchersInWorld = punchersIn(world);
        punchersInWorld.remove(puncher.getUUID());
    }

    public Map<UUID, ICircuitPuncher> punchersIn(LevelAccessor world) {
        if (!connections.containsKey(world)) {
            if (!(world instanceof PonderLevel)) {
                Destroy.LOGGER.warn("Tried to access unprepared circuit punching space of " + WorldHelper.getDimensionID(world));
            }
            return new HashMap<>();
        }
        return connections.get(world);
    }
}
