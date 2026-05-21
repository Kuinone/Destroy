package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import petrolpark.mc.destroy.core.explosion.SmartExplosion;

/**
 * Marker interface for Items that enact custom effects when embedded in a mixed-explosive mix and
 * detonated. Invoked <b>after</b> block-removal (client side) so effects can reference the final
 * destroyed-block list.
 *
 * <p>Consumers (S204 immediate + S207 wiring):</p>
 * <ul>
 * <li>{@link MixedExplosiveInventory#getSpecialItems} (S204) — filters inventory for stacks
 * whose item is either vanilla {@code FireworkStarItem} or this interface.</li>
 * <li>{@link CustomExplosiveMixExplosion#effects} (S207) — dispatches to {@link #explode}
 * on each special item stack after block removal (clientSide only).</li>
 * </ul>
*/
public interface ISpecialEffectExplosiveItem {

    /**
 * Enact the special effects of an Item when being exploded. This occurs after all the blocks
 * have been removed, on the client side.
 *
 * @param explosion the {@link SmartExplosion} driving this detonation
 * @param level the level where the explosion happened
 * @param toBlow positions of all blocks which got removed (from {@code Explosion.getToBlow()})
 * @param specialItemStack the stack in the explosive mix carrying this marker interface
*/
    void explode(SmartExplosion explosion, Level level, List<BlockPos> toBlow, ItemStack specialItemStack);
}
