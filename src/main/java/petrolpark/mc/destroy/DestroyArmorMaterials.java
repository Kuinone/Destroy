package petrolpark.mc.destroy;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Destroy's armor materials.
 *
 * <p>Constructor layout ({@code ArmorMaterial}):</p>
 * <pre>
 * EnumMap&lt;ArmorItem.Type, Integer&gt; defense,
 * int enchantmentValue,
 * Holder&lt;SoundEvent&gt; equipSound,
 * Supplier&lt;Ingredient&gt; repairIngredient,
 * List&lt;ArmorMaterial.Layer&gt; layers,
 * float toughness,
 * float knockbackResistance
 * </pre>
 *
 * <p>Durability is <b>not</b> on the material — it is expressed per-slot via
 * {@code ArmorItem.Type.X.getDurability(multiplier)} when building the item properties, or
 * overridden at the Item level (as Destroy does for protection headwear via
 * {@code ChemistryProtectionHeadwearItem#getMaxDamage}).</p>
*/
public class DestroyArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
        DeferredRegister.create(Registries.ARMOR_MATERIAL, Destroy.MOD_ID);

    private static final int[] HAZMAT_DEFENSE = {1, 1, 1, 1}; // BOOTS, LEGGINGS, CHESTPLATE, HELMET

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT = ARMOR_MATERIALS.register(
        "hazmat",
        () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS,      HAZMAT_DEFENSE[0]);
                map.put(ArmorItem.Type.LEGGINGS,   HAZMAT_DEFENSE[1]);
                map.put(ArmorItem.Type.CHESTPLATE, HAZMAT_DEFENSE[2]);
                map.put(ArmorItem.Type.HELMET,     HAZMAT_DEFENSE[3]);
                map.put(ArmorItem.Type.BODY,       0); // animal armor slot; unused but key required
            }),
            0, // enchantmentValue (legacy DestroyArmorMaterials had 0)
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(DestroyTags.Items.TEXTILE_PLASTICS.tag),
            List.of(new ArmorMaterial.Layer(Destroy.asResource("hazmat"))),
            0f, // toughness
            0f  // knockback resistance
        )
    );

    public static void register(IEventBus modEventBus) {
        ARMOR_MATERIALS.register(modEventBus);
    }

    /** Legacy no-arg call kept so existing {@link Destroy} bootstrap still compiles; delegates to the bus-aware variant when invoked from the mod ctor.*/
    public static void register() {
        // class-load trigger; the real bus registration happens via register(IEventBus)
    }
}
