package petrolpark.mc.destroy.content.processing.trypolithography;

import java.util.Optional;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import petrolpark.mc.destroy.client.DestroyGuiTextures;

/**
 * Circuit board intermediate item — a {@link CircuitPatternItem} subclass carrying a circuit
 * pattern for the trypolithography (circuit fabrication) pipeline. Hover tooltip displays the
 * packed 4×4 pattern as a {@link CircuitPatternTooltipComponent} with circuit-board-specific
 * border/cell/shading textures.
*/
public class CircuitBoardItem extends CircuitPatternItem {

    public CircuitBoardItem(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new CircuitPatternTooltipComponent(
            getPattern(stack), true,
            DestroyGuiTextures.CIRCUIT_BOARD_BORDER,
            DestroyGuiTextures.CIRCUIT_BOARD_CELL,
            DestroyGuiTextures.CIRCUIT_BOARD_CELL_SHADING));
    }
}
