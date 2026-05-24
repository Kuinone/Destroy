package petrolpark.mc.destroy.core.chemistry.data;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

/**
 * Server → client packet: sync the datapack-defined molecule set so multiplayer clients can
 * use them in JEI, mixture tooltips, and any LegacySpecies.getMolecule() lookups.
 *
 * <p>Sent from {@link MoleculeDataReloadListener#apply} (broadcast after reload) and from the
 * per-player {@code OnDatapackSyncEvent} handler in {@code DestroyCommonEvents} (so late-joining
 * players also receive the current set).</p>
 *
 * <p>In single player the server-side apply already populates {@code MOLECULES}; the client-side
 * handler is therefore mostly a no-op in SP but is essential in dedicated-server play.</p>
 */
public record SyncMoleculesS2CPacket(Map<ResourceLocation, MoleculeDefinition> molecules)
    implements ClientboundPacketPayload {

    @SuppressWarnings("unused")
    private static final Codec<Map.Entry<ResourceLocation, MoleculeDefinition>> ENTRY_CODEC =
        RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Map.Entry::getKey),
            MoleculeDefinition.CODEC.fieldOf("def").forGetter(Map.Entry::getValue)
        ).apply(i, Map::entry));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMoleculesS2CPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncMoleculesS2CPacket decode(RegistryFriendlyByteBuf buffer) {
                int count = buffer.readVarInt();
                Map<ResourceLocation, MoleculeDefinition> map = new LinkedHashMap<>(count);
                for (int i = 0; i < count; i++) {
                    ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
                    MoleculeDefinition def = ByteBufCodecs.fromCodec(MoleculeDefinition.CODEC).decode(buffer);
                    map.put(id, def);
                }
                return new SyncMoleculesS2CPacket(map);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, SyncMoleculesS2CPacket packet) {
                buffer.writeVarInt(packet.molecules.size());
                for (Map.Entry<ResourceLocation, MoleculeDefinition> e : packet.molecules.entrySet()) {
                    ResourceLocation.STREAM_CODEC.encode(buffer, e.getKey());
                    ByteBufCodecs.fromCodec(MoleculeDefinition.CODEC).encode(buffer, e.getValue());
                }
            }
        };

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SYNC_MOLECULES;
    }

    @Override
    public void handle(LocalPlayer player) {
        // Single-player: both sides share MOLECULES; clear-and-rebuild is redundant but harmless.
        // Dedicated server: client needs this to know the molecule set.
        LegacySpecies.clearDatapackMolecules();
        int ok = 0;
        int skipped = 0;
        for (Map.Entry<ResourceLocation, MoleculeDefinition> entry : molecules.entrySet()) {
            try {
                if (entry.getValue().apply(entry.getKey())) ok++;
                else skipped++;
            } catch (Throwable t) {
                Destroy.LOGGER.warn("Failed to apply synced datapack molecule {}: {}",
                    entry.getKey(), t.getMessage());
                skipped++;
            }
        }
        Destroy.LOGGER.info("Received {} datapack molecule(s) from server; {} applied, {} skipped.",
            molecules.size(), ok, skipped);
    }
}
