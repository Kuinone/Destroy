package petrolpark.mc.destroy.core.item.tooltip;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;

import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Item 实现此接口表示它拥有动态 tooltip（description 在 runtime 根据配置重新算），与普通 Create item 的
 * 固定 lang-key-based tooltip 不同。Modifier 在每次 locale 变化时调用 {@link #getItemDescription()} 重建
 * tooltip，允许基于 {@code DestroyAllConfigs.SERVER.substances.*.get()} 等配置值插入数据。
*/
public interface IDynamicItemDescription {

    /** Returns a TooltipModifier that wraps this dynamic description, or null if item doesn't implement.*/
    static Modifier create(Item item) {
        if (item instanceof IDynamicItemDescription dynamic) return new Modifier(item, dynamic);
        return null;
    }

    ItemDescription getItemDescription();

    Palette getPalette();

    class Modifier extends ItemDescription.Modifier {

        private final IDynamicItemDescription itemWithDescription;

        public Modifier(Item item, IDynamicItemDescription itemWithDescription) {
            super(item, itemWithDescription.getPalette());
            this.itemWithDescription = itemWithDescription;
        }

        @Override
        public void modify(ItemTooltipEvent context) {
            if (checkLocale()) {
                description = itemWithDescription.getItemDescription();
            }
            if (description == null) return;
            context.getToolTip().addAll(1, description.getCurrentLines());
        }
    }
}
