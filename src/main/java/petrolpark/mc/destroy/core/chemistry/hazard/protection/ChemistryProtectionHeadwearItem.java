package petrolpark.mc.destroy.core.chemistry.hazard.protection;

import java.util.function.Supplier;

import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import petrolpark.mc.destroy.core.chemistry.hazard.ChemistryHazardHelper;

/**
 * Headwear PPE — goggles and masks. Implements {@link Equipable} so right-click auto-equips to
 * the head slot; durability / repair / enchantability wired via {@link NonNullConsumer} transforms
 * in {@code DestroyItems}.
*/
public class ChemistryProtectionHeadwearItem extends Item implements Equipable {

    protected Supplier<ConfigBase.ConfigInt> configuredDurability;
    protected boolean goggles;
    protected Supplier<Ingredient> repairMaterial;
    protected boolean enchantable;

    public ChemistryProtectionHeadwearItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);

        configuredDurability = null;
        goggles = false;
        repairMaterial = () -> Ingredient.EMPTY;
        enchantable = false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof LivingEntity livingEntity) {
            if (!ItemStack.matches(livingEntity.getItemBySlot(EquipmentSlot.HEAD), stack)) ChemistryHazardHelper.decontaminate(stack);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack pStack, ItemStack repair) {
        return repairMaterial.get().test(repair);
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return enchantable;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return configuredDurability == null ? 100 : configuredDurability.get().get();
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    public Ingredient getRepairIngredient() {
        return repairMaterial.get();
    }

    public static NonNullConsumer<ChemistryProtectionHeadwearItem> goggles() {
        return item -> {
            item.goggles = true;
            GogglesItem.addIsWearingPredicate(player -> player.getItemBySlot(EquipmentSlot.HEAD).is(item));
        };
    }

    public static NonNullConsumer<ChemistryProtectionHeadwearItem> durability(Supplier<ConfigBase.ConfigInt> durability) {
        return item -> item.configuredDurability = durability;
    }

    public static NonNullConsumer<ChemistryProtectionHeadwearItem> repairIngredient(Supplier<Ingredient> ingredient) {
        return item -> item.repairMaterial = ingredient;
    }

    public static NonNullConsumer<ChemistryProtectionHeadwearItem> enchantable() {
        return item -> item.enchantable = true;
    }
}
