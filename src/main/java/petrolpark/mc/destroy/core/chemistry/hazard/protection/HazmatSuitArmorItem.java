package petrolpark.mc.destroy.core.chemistry.hazard.protection;

import com.simibubi.create.content.equipment.armor.BaseArmorItem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyArmorMaterials;
import petrolpark.mc.destroy.core.chemistry.hazard.ChemistryHazardHelper;

/**
 * Hazmat suit (chest/legs/boots). Extends Create's {@link BaseArmorItem} which in 1.21.1 takes
 * {@code (Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ResourceLocation)} — the texture
 * {@code ResourceLocation} is Create's override for layer-1/layer-2 armor texture lookup.
*/
public class HazmatSuitArmorItem extends BaseArmorItem {

    public HazmatSuitArmorItem(ArmorItem.Type type, Properties properties) {
        super(DestroyArmorMaterials.HAZMAT, type, properties, Destroy.asResource("hazmat"));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof LivingEntity livingEntity) {
            if (!ItemStack.matches(livingEntity.getItemBySlot(this.getEquipmentSlot()), stack))
                ChemistryHazardHelper.decontaminate(stack);
        }
    }
}
