package petrolpark.mc.destroy;

import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import petrolpark.mc.destroy.content.processing.trypolithography.recipe.CircuitPatternIngredient;

/**
 * Registry entry for all Destroy {@link IngredientType custom item ingredients}. Sibling to
 * {@link DestroyFluidIngredientTypes} (which handles fluid ingredients on the
 * {@code FLUID_INGREDIENT_TYPES} registry); this class targets the parallel
 * {@link NeoForgeRegistries.Keys#INGREDIENT_TYPES} registry for item-level ingredient codecs.
 *
 * <p>Registry IDs:</p>
 * <ul>
 * <li>{@code destroy:circuit_pattern_item} — {@link CircuitPatternIngredient} (matches a
 * {@link petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItem} stack
 * carrying a specific server-resolved pattern)</li>
 * </ul>
*/
public class DestroyIngredientTypes {

    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Destroy.MOD_ID);

    public static final Supplier<IngredientType<CircuitPatternIngredient>> CIRCUIT_PATTERN_ITEM =
        INGREDIENT_TYPES.register("circuit_pattern_item",
            () -> new IngredientType<>(CircuitPatternIngredient.CODEC, CircuitPatternIngredient.STREAM_CODEC));

    // MaskIngredient registration rolled back (user审计 deemed JEI punched-mask display
    // not worth the ICustomIngredient infrastructure complexity). Recipe slots now show plain
    // blank masks via vanilla {@code Ingredient.of(maskItem)} — minor visual concession,
    // functionally identical for actual crafting.

    public static void register(IEventBus modEventBus) {
        INGREDIENT_TYPES.register(modEventBus);
    }
}
