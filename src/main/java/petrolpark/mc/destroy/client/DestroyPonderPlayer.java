package petrolpark.mc.destroy.client;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.createmod.catnip.levelWrappers.WrappedClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.level.Level;

/**
 * Destroy-side workaround for petrolpark library 1.4.31 {@code PonderPlayer.<init>} bug. The
 * upstream class does {@code new GameProfile(null, name)} which 1.21 authlib (6.0.54) rejects
 * via {@code Objects.requireNonNull}, causing every Ponder scene that spawns a player figure
 * (Blowpipe / Pumpjack / Pollution scenes) to crash on tick.
 *
 * <p>Fix: subclass {@link AbstractClientPlayer} directly (skip the buggy parent) and supply a
 * deterministic synthetic UUID derived from the player's scoreboard name. UUID just needs to be
 * non-null for the GameProfile contract; functionally interchangeable for ponder display.</p>
 *
 * <ul>
 * <li>{@code ProcessingPonderScenes#blowpipe} (line 755)</li>
 * <li>{@code OilPonderScenes} (line 512, Pumpjack-related)</li>
 * <li>{@code PollutionPonderScenes} (lines 426 + 500)</li>
 * </ul>
 *
 * <p>When petrolpark library ships a fix (e.g. 1.4.32+), this class can be removed and call
 * sites reverted to {@code new PonderPlayer(w, name)}.</p>
*/
public class DestroyPonderPlayer extends AbstractClientPlayer {

    public DestroyPonderPlayer(Level level, String playername) {
        super(WrappedClientLevel.of(level),
            // synthetic UUID derived from name keeps GameProfile contract happy.
            // Mojang's java.util.UUID#nameUUIDFromBytes returns a deterministic UUID for a
            // given name, so the same scoreboard name always maps to the same profile.
            new GameProfile(UUID.nameUUIDFromBytes(("DestroyPonderPlayer:" + playername).getBytes()), playername));
    }
}
