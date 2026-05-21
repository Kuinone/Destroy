package petrolpark.mc.destroy.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternTooltipComponent;
import petrolpark.mc.destroy.core.chemistry.MoleculeDisplayItem;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosivePropertiesTooltip;

/** 1.21 NeoForge requires this via
 * {@link RegisterClientTooltipComponentFactoriesEvent}; unregistered types crash with
 * {@code "Unknown TooltipComponent"} when the tooltip layer tries to render.
*/
@EventBusSubscriber(modid = Destroy.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class DestroyClientTooltips {

    @SubscribeEvent
    public static void register(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MoleculeDisplayItem.MoleculeTooltip.class,
            MoleculeDisplayItem.MoleculeTooltip::getClientTooltipComponent);
        event.register(CircuitPatternTooltipComponent.class,
            CircuitPatternTooltipComponent::getClientTooltipComponent);
        // ExplosivePropertiesTooltip drives the explosives-chart hover image on
        // IMixedExplosiveItem stacks (DestroyTooltipEvents.onGatherTooltips attaches it).
        event.register(ExplosivePropertiesTooltip.class,
            ExplosivePropertiesTooltip::getClientTooltipComponent);
    }
}
