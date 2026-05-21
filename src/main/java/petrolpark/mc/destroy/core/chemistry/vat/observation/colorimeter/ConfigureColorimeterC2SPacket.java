package petrolpark.mc.destroy.core.chemistry.vat.observation.colorimeter;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

/**
 * Client → server packet: configure the Colorimeter's observed molecule + phase (gas vs liquid).
 * Sent by {@link ColorimeterScreen#onClose} when the player closes the GUI after editing the
 * species selector or phase toggle.
*/
public record ConfigureColorimeterC2SPacket(boolean observingGas, String speciesId, BlockPos pos)
    implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureColorimeterC2SPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, ConfigureColorimeterC2SPacket::observingGas,
            ByteBufCodecs.STRING_UTF8, ConfigureColorimeterC2SPacket::speciesId,
            BlockPos.STREAM_CODEC, ConfigureColorimeterC2SPacket::pos,
            ConfigureColorimeterC2SPacket::new);

    /** Factory that accepts a nullable LegacySpecies (client-side call site convenience).*/
    public static ConfigureColorimeterC2SPacket of(boolean observingGas, LegacySpecies species, BlockPos pos) {
        return new ConfigureColorimeterC2SPacket(observingGas, species == null ? "" : species.getFullID(), pos);
    }

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CONFIGURE_COLORIMETER;
    }

    @Override
    public void handle(ServerPlayer player) {
        LegacySpecies species = speciesId.isEmpty() ? null : LegacySpecies.getMolecule(speciesId);
        player.level().getBlockEntity(pos, DestroyBlockEntityTypes.COLORIMETER.get())
            .ifPresent(be -> be.configure(species, observingGas));
    }
}
