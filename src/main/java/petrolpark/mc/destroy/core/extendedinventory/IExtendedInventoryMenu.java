package petrolpark.mc.destroy.core.extendedinventory;

/**
 * Marker interface for {@link net.minecraft.world.inventory.AbstractContainerMenu Menu}s that
 * add the Extended-Inventory slots themselves (instead of relying on
 * {@link ExtendedInventory#onOpenContainer} auto-injection). Server-side the location of these
 * slots doesn't matter — they're indexed via {@link ExtendedInventory#getExtraInventoryStartSlotIndex}.
*/
public interface IExtendedInventoryMenu {
}
