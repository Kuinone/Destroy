package petrolpark.mc.destroy.core.item.tooltip;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * Abstract base for Destroy's 2-layer tooltip components — server-safe {@link TooltipComponent}
 * (carries data + marker interface) + paired client-only {@link ClientTooltipComponent} (renders
 * the image on GuiGraphics).
*/
public abstract class DestroyTooltipComponent<T extends DestroyTooltipComponent<?, ?>, C extends ClientTooltipComponent> implements TooltipComponent {

    public abstract C getClientTooltipComponent();
}
