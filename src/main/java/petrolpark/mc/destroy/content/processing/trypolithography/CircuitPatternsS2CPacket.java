package petrolpark.mc.destroy.content.processing.trypolithography;

import java.util.HashMap;
import java.util.Map;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyPackets;

/**
 * Server → client packet: sync the complete {@code ResourceLocation → packed-pattern} map from the
 * server's {@code CircuitPatternHandler} to the client (so client-side recipe display can show
 * each pattern's current 4×4 bitmask). Sent on resource-pack reload and on world load.
*/
public record CircuitPatternsS2CPacket(Map<ResourceLocation, Integer> patterns) implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, CircuitPatternsS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT), CircuitPatternsS2CPacket::patterns,
        CircuitPatternsS2CPacket::new);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CIRCUIT_PATTERNS;
    }

    @Override
    public void handle(LocalPlayer player) {
        // CircuitPatternHandler ported; wire the client-side cache update.
        // Note: CircuitPatternHandler is a server-side SavedData, but setPatterns() only writes
        // the GENERATED_PATTERNS map (no server-only fields touched). Safe to call on client.
        Destroy.CIRCUIT_PATTERN_HANDLER.setPatterns(patterns);
    }
}
