package petrolpark.mc.destroy.core.chemistry.hazard;

import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;

import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.client.DestroyLang;

/**
 * Adds a contamination-warning tooltip line to items that carry a {@code CONTAMINATING_FLUID}
 * DataComponent. Alt-held state also reveals the specific FluidStack display name in red.
 *
 * <ul>
 * <li>{@code stack.hasTag() + tag.contains("ContaminatingFluid", Tag.TAG_COMPOUND)} →
 * {@code stack.has(DestroyDataComponents.CONTAMINATING_FLUID)} (single typed check)</li>
 * <li>{@code FluidStack.loadFluidStackFromNBT(tag.getCompound(...))} →
 * {@code stack.get(DestroyDataComponents.CONTAMINATING_FLUID)} (the component <em>is</em> a FluidStack)</li>
 * <li>{@code net.minecraftforge.event.entity.player.ItemTooltipEvent} →
 * {@code net.neoforged.neoforge.event.entity.player.ItemTooltipEvent} (NeoForge package path)</li>
 * </ul>
*/
public class ContaminatedItemTooltipModifier implements TooltipModifier {

    public static final Palette DARK_GRAY_AND_WHITE = Palette.ofColors(ChatFormatting.DARK_GRAY, ChatFormatting.WHITE);

    @Override
    public void modify(ItemTooltipEvent context) {
        FluidStack fluid = context.getItemStack().get(DestroyDataComponents.CONTAMINATING_FLUID);
        if (fluid == null) return;

        context.getToolTip().addAll(1, TooltipHelper.cutTextComponent(
            DestroyLang.translate("tooltip.contamination_description").component(),
            Screen.hasAltDown() ? DARK_GRAY_AND_WHITE : Palette.GRAY));

        if (!fluid.isEmpty() && Screen.hasAltDown()) {
            context.getToolTip().add(2, Component.literal(" "));
            context.getToolTip().add(3, Component.literal(" "));
            context.getToolTip().addAll(3, TooltipHelper.cutTextComponent(
                DestroyLang.translate("tooltip.contamination", fluid.getHoverName()).component(),
                Palette.RED));
        }
    }
}
