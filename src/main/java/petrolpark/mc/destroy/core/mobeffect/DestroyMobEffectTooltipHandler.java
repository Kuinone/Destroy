package petrolpark.mc.destroy.core.mobeffect;

import java.util.List;

import com.simibubi.create.foundation.item.TooltipHelper;

import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import petrolpark.mc.destroy.Destroy;

/**
 * Defer-B closure — DestroyMobEffect tooltip description restoration.
 *
 * <p>Registered via {@code @EventBusSubscriber(Dist.CLIENT)} — fires on the game event bus
 * (default) for {@link ItemTooltipEvent}. Client-only handler (no server-side tooltip).</p>
*/
@EventBusSubscriber(value = Dist.CLIENT, modid = Destroy.MOD_ID)
public class DestroyMobEffectTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return;

        List<Component> tooltip = event.getToolTip();
        boolean addedAny = false;

        // Iterate all effects in the potion (includes base potion + custom effects)
        for (MobEffectInstance instance : (Iterable<MobEffectInstance>) potionContents.getAllEffects()) {
            MobEffect effect = instance.getEffect().value();
            if (!(effect instanceof DestroyMobEffect)) continue;

            String descriptionKey = instance.getDescriptionId() + ".description";
            if (!I18n.exists(descriptionKey)) continue;

            if (!addedAny) {
                tooltip.add(Component.literal(""));  // blank line separator before descriptions
                addedAny = true;
            }
            tooltip.addAll(TooltipHelper.cutTextComponent(Component.translatable(descriptionKey), Palette.GRAY));
        }
    }
}
