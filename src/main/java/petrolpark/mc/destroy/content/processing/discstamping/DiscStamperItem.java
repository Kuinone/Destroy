package petrolpark.mc.destroy.content.processing.discstamping;

import java.util.List;

import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.TooltipFlag;

import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.core.item.WithSecondaryItem;

/**
 * Disc Stamper item — a reusable tool that stamps a specific music disc pattern onto a
 * {@code BLANK_MUSIC_DISC} via a Create Deployer. Each stamper carries one music disc as a
 * {@link DestroyDataComponents#STAMPED_DISC} DataComponent; the Deployer application recipe
 * uses the stamper's stored disc as the output.
*/
public class DiscStamperItem extends WithSecondaryItem {

    public DiscStamperItem(@NonnullType Properties properties) {
        super(properties, DiscStamperItem::getDisc);
    }

    /**
 * Read the stamped disc from a stamper stack. Returns {@link ItemStack#EMPTY} if the stamper
 * has no stamped disc (freshly crafted blank stamper).
*/
    public static ItemStack getDisc(ItemStack stamper) {
        return stamper.getOrDefault(DestroyDataComponents.STAMPED_DISC, ItemStack.EMPTY);
    }

    /**
 * Build a DISC_STAMPER stack with the given disc stamped into its {@code STAMPED_DISC}
 * DataComponent. Used by {@link DiscElectroplatingRecipe#copyWithDisc} to produce stamper
 * outputs in electroplating recipes.
*/
    public static ItemStack of(ItemStack discStack) {
        ItemStack stack = DestroyItems.DISC_STAMPER.asStack();
        stack.set(DestroyDataComponents.STAMPED_DISC, discStack);
        return stack;
    }

    /**
 * 1.21 vanilla refactor: music discs are plain {@link Item}s with song metadata in
 * {@link DataComponents#JUKEBOX_PLAYABLE} — a {@link JukeboxPlayable} record that **already**
 * has {@link JukeboxPlayable#addToTooltip(Item.TooltipContext, java.util.function.Consumer, TooltipFlag)}
 * which formats the song description line vanilla-style. Just delegate to it + show the disc's
 * own display name first.
 *
 * <p>User-visible behavior in JEI / inventory hover:</p>
 * <ul>
 * <li>line 1: "Music Disc" (or the disc's localized name) in gray</li>
 * <li>line 2 (via JukeboxPlayable.addToTooltip): "C418 - Cat" / "Lena Raine - Pigstep" / etc.</li>
 * </ul>
 *
 * <p>Without this, JEI hover on disc_stamper just showed item name + "destroy:disc_stamper" id
 * with no hint of which disc is encoded — user complaint "压模没有显示它复制的是哪种唱片".</p>
*/
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
        ItemStack disc = getDisc(stack);
        if (disc.isEmpty()) return;
        // Show the disc's own display name (e.g. "Music Disc") in gray — matches the visual weight
        tooltip.add(disc.getHoverName().copy().withStyle(ChatFormatting.GRAY));
        // Delegate the song description ("C418 - Cat" etc.) to vanilla's JukeboxPlayable formatter.
        JukeboxPlayable jukeboxPlayable = disc.get(DataComponents.JUKEBOX_PLAYABLE);
        if (jukeboxPlayable != null) {
            jukeboxPlayable.addToTooltip(context, tooltip::add, isAdvanced);
        }
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return itemStack;
    }
}
