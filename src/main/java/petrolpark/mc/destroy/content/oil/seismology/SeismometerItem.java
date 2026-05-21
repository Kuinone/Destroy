package petrolpark.mc.destroy.content.oil.seismology;

import java.util.List;
import java.util.stream.Stream;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.content.oil.ChunkCrudeOil;
import petrolpark.mc.destroy.content.oil.seismology.SeismographItem.Seismograph;

/**
 * Handheld Seismometer — triggered by nearby explosions. For each nearby Player carrying a
 * Seismometer, scans their inventory for {@link SeismographItem}s centered on the current 8-chunk
 * region and fills them with the ChunkCrudeOil-driven nonogram signals (see
 * {@link ChunkCrudeOil#getSignals}), awarding XP if the explosion actually hit an oil-bearing
 * chunk.
*/
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class SeismometerItem extends Item {

    public SeismometerItem(Properties properties) {
        super(properties);
    }

    /**
 * Trigger handheld Seismometers when there are nearby explosions (within a 16-block cube of
 * the explosion origin).
*/
    @SubscribeEvent
    public static final void onExplosion(ExplosionEvent.Start event) {
        Level level = event.getLevel();
        level.getEntitiesOfClass(Player.class, AABB.ofSize(event.getExplosion().center(), 16, 16, 16), player -> true).forEach(player -> {
            if (!player.getInventory().hasAnyMatching(DestroyItems.SEISMOMETER::isIn)) return;

            int chunkX = SectionPos.blockToSectionCoord(player.getOnPos().getX());
            int chunkZ = SectionPos.blockToSectionCoord(player.getOnPos().getZ());

            // Not yet ported — fall back to main inventory (36 slots). Hotbar + inventory
            // Seismographs still work; Curios-stored Seismographs temporarily skipped.
            // TODO(ExtendedInventory port): replace with ExtendedInventory.get(player).stream()
            List<ItemStack> seismographs = Stream.concat(
                    player.getInventory().items.stream(),
                    player.getInventory().offhand.stream())
                .filter(DestroyItems.SEISMOGRAPH::isIn)
                .filter(stack -> {
                    MapItemSavedData mapData = MapItem.getSavedData(stack, level);
                    if (mapData == null) return false;
                    return (SeismographItem.mapChunkCenter(chunkX) * 16 == mapData.centerX
                        && SeismographItem.mapChunkCenter(chunkZ) * 16 == mapData.centerZ);
                })
                .toList();

            // Generate the oil in this chunk — AttachmentType is always-present (auto-created on
            LevelChunk chunk = level.getChunk(chunkX, chunkZ);
            ChunkCrudeOil cco = chunk.getData(DestroyAttachmentTypes.CHUNK_CRUDE_OIL);
            int newOilGenerated = 0;
            if (!cco.isGenerated()) {
                cco.generate(chunk, player);
                newOilGenerated = cco.getAmount();
            }

            boolean newInfo = false; // Whether new information was added to any Seismographs

            // Add information to Seismographs and display information to the player (server-side)
            if (level instanceof ServerLevel serverLevel) {
                byte xSignals = ChunkCrudeOil.getSignals(serverLevel, chunkX, chunkZ, true);
                byte zSignals = ChunkCrudeOil.getSignals(serverLevel, chunkX, chunkZ, false);
                int modX = chunkX - SeismographItem.mapChunkLowerCorner(chunkX);
                int modZ = chunkZ - SeismographItem.mapChunkLowerCorner(chunkZ);
                for (ItemStack stack : seismographs) {
                    Seismograph seismograph = SeismographItem.readSeismograph(stack);
                    // Mark this chunk as definitively seismically active or not on the Seismograph
                    newInfo |= seismograph.mark(modX, modZ, (zSignals & 1 << modZ) != 0 ? Seismograph.Mark.TICK : Seismograph.Mark.CROSS);
                    // Add nonogram info
                    newInfo |= seismograph.discoverColumn(modX, level, player);
                    newInfo |= seismograph.discoverRow(modZ, level, player);
                    seismograph.getColumns()[modX] = zSignals;
                    seismograph.getRows()[modZ] = xSignals;
                    SeismographItem.writeSeismograph(stack, seismograph);
                }
                // Show message (and award XP if necessary)
                final int oilFound = newOilGenerated;
                if (oilFound > 0) {
                    player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.struck_oil", oilFound / 1000).component(), true);
                    ExperienceOrb.award(serverLevel, player.position(), oilFound / 10000);
                } else if (seismographs.isEmpty()) {
                    player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.no_seismograph").style(ChatFormatting.RED).component(), true);
                } else if (newInfo) {
                    player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.added_info").component(), true);
                } else {
                    player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.no_new_info").style(ChatFormatting.RED).component(), true);
                }
            }

            // Update the spike animation of the Seismometer(s) — catnip sendToClient(player, packet)
            // argument order.
            if (player instanceof ServerPlayer serverPlayer) {
                CatnipServices.NETWORK.sendToClient(serverPlayer, SeismometerSpikeS2CPacket.INSTANCE);
            }
            // Award advancement if some Seismograph info was filled in
            if (newInfo) DestroyAdvancementTrigger.USE_SEISMOMETER.award(level, player);
        });
    }
}
