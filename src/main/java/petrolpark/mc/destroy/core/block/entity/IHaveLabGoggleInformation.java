package petrolpark.mc.destroy.core.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;

import net.minecraft.world.item.ItemStack;

import petrolpark.mc.destroy.DestroyItems;

/**
 * Mix-in over Create's {@link IHaveGoggleInformation}: when the goggle tooltip's overlay icon is
 * queried, return Destroy's Laboratory Goggles rather than Create's default spyglass. Used by
 * Destroy machines whose tooltip is only meaningful when the player is wearing Destroy's lab
 * goggles.
*/
public interface IHaveLabGoggleInformation extends IHaveGoggleInformation {

    @Override
    default ItemStack getIcon(boolean isPlayerSneaking) {
        return DestroyItems.LABORATORY_GOGGLES.asStack();
    }
}
