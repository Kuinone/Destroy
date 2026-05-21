package petrolpark.mc.destroy.core.chemistry.hazard;

import javax.annotation.Nullable;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

/**
 * Notifies the client which Molecule is poisoning the local player (for HUD + death message).
 *
 * <p>Wired up via {@link DestroyPackets#CHEMICAL_POISON}.</p>
*/
public record ChemicalPoisonS2CPacket(String moleculeId) implements ClientboundPacketPayload {

    private static final String SENTINEL_NO_MOLECULE = "NO_MOLECULE";

    public ChemicalPoisonS2CPacket(@Nullable LegacySpecies molecule) {
        this(molecule == null ? SENTINEL_NO_MOLECULE : molecule.getFullID());
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalPoisonS2CPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ChemicalPoisonS2CPacket::moleculeId,
            ChemicalPoisonS2CPacket::new
        );

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CHEMICAL_POISON;
    }

    @Override
    public void handle(LocalPlayer ignored) {
        // Use the globally-authoritative Minecraft.player rather than the handler's passed-in player
        // preserve byte-for-byte semantics.
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        LegacySpecies molecule = SENTINEL_NO_MOLECULE.equals(moleculeId) ? null : LegacySpecies.getMolecule(moleculeId);
        minecraft.player.getData(DestroyAttachmentTypes.ENTITY_CHEMICAL_POISON).setMoleculeDirect(molecule);
    }
}
