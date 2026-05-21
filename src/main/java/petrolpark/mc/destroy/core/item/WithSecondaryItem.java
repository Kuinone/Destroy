package petrolpark.mc.destroy.core.item;

import java.util.function.UnaryOperator;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Items which have a secondary icon on the bottom right in a GUI when shift is held. The Create renderer pipeline still
 * exists in Create 6.0.8, but {@code ItemRenderer#renderStatic(...)} / {@code render(...)}
 * signatures changed in 1.21.1 (new {@code ItemDisplayContext}/{@code TransformType} flow +
 * {@code ModelData} parameter). Porting the renderer is deferred — the secondary-icon overlay
 * is temporarily lost while the items remain functional for tags/recipes/tooltips.
 *
 * <p>Server-side behaviour ({@link #getSecondaryItem(ItemStack)}) is unchanged so hefty-beetroot
 * harvest drops / JEI category extractions can still call in.
*/
public class WithSecondaryItem extends Item {

    private final UnaryOperator<ItemStack> secondaryItemSupplier;

    public WithSecondaryItem(@NonnullType Properties properties, UnaryOperator<ItemStack> secondaryItem) {
        super(properties);
        this.secondaryItemSupplier = secondaryItem;
    }

    public static ItemStack getSecondaryItem(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof WithSecondaryItem item)) return ItemStack.EMPTY;
        return item.secondaryItemSupplier.apply(itemStack);
    }

    // TODO(client-render): port WithSecondaryItemRenderer — needs 1.21.1 ItemRenderer signatures
    // + Create 6.0.8 CustomRenderedItemModelRenderer shape. Track in DestroyItemProperties batch.
}
