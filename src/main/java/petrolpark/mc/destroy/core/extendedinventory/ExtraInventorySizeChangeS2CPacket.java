package petrolpark.mc.destroy.core.extendedinventory;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import petrolpark.mc.destroy.DestroyPackets;

/**
 * Server → client: tells the receiving Player's {@link ExtendedInventory} its new sizing
 * ({@code extraInventorySize} + {@code extraHotbarSlots}). Server fires this whenever
 * {@code ExtendedInventory.setExtraInventorySize} or {@code setExtraHotbarSlots} runs
 * (most commonly: Creatine consumption applies an attribute modifier → the
 * {@link petrolpark.mc.destroy.DestroyAttributes#EXTRA_INVENTORY_SIZE} attribute changes →
 * {@code ExtendedInventory.updateSize} re-syncs).
 *
 * <p>The {@code requestFullState} flag tells the client to round-trip a
 * {@link RequestInventoryFullStateC2SPacket} back to the server after applying the size change,
 * forcing a full menu re-broadcast — used when the size grows so the client can populate the
 * newly-allocated slot indexes with their actual server-side contents.</p>
*/
public record ExtraInventorySizeChangeS2CPacket(int extraInventorySize, int extraHotbarSlots,
                                                boolean requestFullState)
    implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ExtraInventorySizeChangeS2CPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ExtraInventorySizeChangeS2CPacket::extraInventorySize,
            ByteBufCodecs.VAR_INT, ExtraInventorySizeChangeS2CPacket::extraHotbarSlots,
            ByteBufCodecs.BOOL, ExtraInventorySizeChangeS2CPacket::requestFullState,
            ExtraInventorySizeChangeS2CPacket::new);

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.EXTRA_INVENTORY_SIZE_CHANGE;
    }

    @Override
    public void handle(LocalPlayer player) {
        // delegate to the full client handler. This now ALSO calls
        // refreshClientInventoryMenu(inv) which rebuilds the player's inventoryMenu with extra
        // slots positioned per current screen geometry, so the next inventory open shows them.
        petrolpark.mc.destroy.client.ExtendedInventoryClientHandler
            .handleExtendedInventorySizeChange(this);
    }
}
