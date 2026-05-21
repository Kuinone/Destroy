package petrolpark.mc.destroy.core.item.tooltip;

import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;

import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import petrolpark.mc.destroy.DestroyTags;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.config.DestroyAllConfigs;

/**
 * Adds a "liable to change" red-flagged notice to any item tagged
 * {@code destroy:liable_to_change}, gated by {@code CLIENT.tempramentalItemDescriptions} config.

*/
public class TempramentalItemDescription implements TooltipModifier {

    @Override
    public void modify(ItemTooltipEvent context) {
        if (!DestroyAllConfigs.CLIENT.tempramentalItemDescriptions.get()) return;
        if (!DestroyTags.Items.LIABLE_TO_CHANGE.matches(context.getItemStack().getItem())) return;
        context.getToolTip().add(Component.literal(" "));
        context.getToolTip().addAll(TooltipHelper.cutTextComponent(
            DestroyLang.translate("tooltip.liable_to_change").component(), Palette.RED));
    }
}
