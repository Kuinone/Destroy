package petrolpark.mc.destroy.core.block.entity;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Marker mix-in for block-entities that want to run a side-effect when the local player looks at
 * them (client side). Typical uses: drawing a temporary block-outline around an assembled
 * multi-block, flashing highlight overlays, or surfacing additional tooltip context. The hook
 * fires each tick the player's crosshair is over the block.
*/
public interface ISpecialWhenHoveredBlockEntity {

    void whenLookedAt(LocalPlayer player, BlockHitResult result);
}
