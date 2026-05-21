package petrolpark.mc.destroy.compat.jei;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import petrolpark.mc.destroy.client.IConditionalGhostSlot;

/** Mirrors
 * Create's {@link com.simibubi.create.compat.jei.GhostIngredientHandler} but adds a defensive
 * {@link IConditionalGhostSlot#isValid} check (skip slots the menu has marked unavailable).
 *
 * <p>Generic type parameter mirrors Create's pattern — concrete handlers register against
 * specific {@code AbstractSimiContainerScreen<? extends GhostItemMenu<?>>} subclasses via
 * {@code registration.addGhostIngredientHandler(ScreenClass.class, new DestroyGhostIngredientHandler())}.</p>
*/
public class DestroyGhostIngredientHandler<T extends GhostItemMenu<?>>
    implements IGhostIngredientHandler<AbstractSimiContainerScreen<T>> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(AbstractSimiContainerScreen<T> gui, ITypedIngredient<I> ingredient,
                                                boolean doStart) {
        List<Target<I>> targets = new LinkedList<>();

        if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
            // Filter only on IConditionalGhostSlot.isValid (NOT slot.isActive()).
            // RedstoneProgrammerMenu.FrequencySlotItemHandler.isActive() is intentionally
            // overridden to return false.
            // Filtering on isActive=false eliminates every ghost slot, leaving 0 drag targets
            // and JEI refusing to start a drag.
            for (int i = 0; i < gui.getMenu().slots.size(); i++) {
                Slot slot = gui.getMenu().slots.get(i);
                if (!(slot instanceof IConditionalGhostSlot conditional)) continue;
                if (!conditional.isValid()) continue;
                targets.add(new DestroyGhostTarget<>(gui, i));
            }
        }

        return targets;
    }

    @Override
    public void onComplete() {}

    @Override
    public boolean shouldHighlightTargets() {
        return true;
    }

    public static class DestroyGhostTarget<I, T extends GhostItemMenu<?>> implements Target<I> {

        private final Rect2i area;
        private final AbstractSimiContainerScreen<T> gui;
        private final int slotIndex;

        public DestroyGhostTarget(AbstractSimiContainerScreen<T> gui, int slotIndex) {
            this.gui = gui;
            this.slotIndex = slotIndex;
            // slotIndex IS the raw menu slot index (no offset, see getTargetsTyped).
            Slot slot = gui.getMenu().slots.get(slotIndex);
            this.area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(I ingredient) {
            ItemStack stack = ((ItemStack) ingredient).copy();
            stack.setCount(1);
            gui.getMenu().ghostInventory.setStackInSlot(slotIndex, stack);
            // AllPackets.getChannel().sendToServer call.
            CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, slotIndex));
        }
    }
}
