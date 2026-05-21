package petrolpark.mc.destroy.content.redstone.programmer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.createmod.catnip.data.WorldAttached;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgrammerBlockItem.ItemStackRedstoneProgram;

/**
 * Server-side registry of {@link ItemStackRedstoneProgram}s for Redstone Programmer
 * {@code BlockItem}s held in player inventories — a per-level map keyed by stack UUID. Programs
 * tick down a TTL each level tick; when TTL expires (stack no longer in any inventory, Player
 * dropped / moved), the program is unloaded from the RedstoneLinkNetworkHandler and removed.
*/
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class RedstoneProgrammerItemHandler {

    public static final WorldAttached<Map<UUID, ItemStackRedstoneProgram>> programs =
        new WorldAttached<>(level -> new HashMap<>());
    public static final int TIMEOUT = 30;

    public static void tick(LevelAccessor level) {
        Map<UUID, ItemStackRedstoneProgram> map = programs.get(level);
        for (Iterator<Entry<UUID, ItemStackRedstoneProgram>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Entry<UUID, ItemStackRedstoneProgram> entry = iterator.next();
            ItemStackRedstoneProgram program = entry.getValue();
            program.ttl--;
            if (!program.shouldTransmit()) {
                program.unload();
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static final void onLevelTick(LevelTickEvent.Post event) {
        tick(event.getLevel());
    }
}
