package petrolpark.mc.destroy.content.processing.trypolithography.keypunch;

import java.util.UUID;

/**
 * Marker interface for something that can punch a {@code CircuitMaskItem} — concretely, the
 * {@code KeypunchBlockEntity} and the {@code CircuitPuncherHandler.UNKNOWN} sentinel. Provides a
 * stable UUID (persisted on the punched mask to track provenance) and a display name (shown in
 * the mask's tooltip "Punched by X" line).
*/
public interface ICircuitPuncher {

    UUID getUUID();

    String getName();
}
