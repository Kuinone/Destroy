package petrolpark.mc.destroy.client;

import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosivePropertiesTooltip;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.IMixedExplosiveItem;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.MixedExplosiveScreen;

/**
 * Intercept vanilla tooltip
 * gather, and for any {@link IMixedExplosiveItem} stack append an {@link ExplosivePropertiesTooltip}
 * component — this renders the 7-row CustomExplosiveChart icon graphic inside the item tooltip
 * showing the stack's explosive-property balance (strength bars, condition checkmarks).
*/
@EventBusSubscriber(modid = Destroy.MOD_ID, value = Dist.CLIENT)
public class DestroyTooltipEvents {

    @SubscribeEvent
    public static void onGatherTooltips(RenderTooltipEvent.GatherComponents event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ItemStack stack = event.getItemStack();
        ExplosiveProperties properties = null;
        if (stack.getItem() instanceof IMixedExplosiveItem mixItem) {
            properties = mixItem.getExplosiveInventory(stack, mc.level.registryAccess())
                .getExplosiveProperties()
                .withConditions(mixItem.getApplicableExplosionConditions());
        } else if (mc.screen instanceof MixedExplosiveScreen) {
            properties = ExplosiveProperties.ITEM_EXPLOSIVE_PROPERTIES.get(stack.getItem());
        }
        if (properties != null) {
            event.getTooltipElements().add(Either.right(new ExplosivePropertiesTooltip(properties)));
        }
    }
}
