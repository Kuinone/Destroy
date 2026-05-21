package petrolpark.mc.destroy.core.explosion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.DestroyTags;

/**
 * Custom Crafting Recipe — combines Card Stock + a Primary or Secondary explosive (+ optional
 * Firework Stars) into a Firework Rocket with extended {@link Fireworks#flightDuration() flight
 * duration} (4 or 5 vs vanilla 1-3). Two variants registered separately so JEI can show them as
 * distinct recipes.
*/
public class ExtendedDurationFireworkRocketRecipe extends CustomRecipe {

    public static final RecipeSerializer<ExtendedDurationFireworkRocketRecipe> DURATION_4_FIREWORK_ROCKET =
        new SimpleCraftingRecipeSerializer<>(c -> new ExtendedDurationFireworkRocketRecipe(c, false));
    public static final RecipeSerializer<ExtendedDurationFireworkRocketRecipe> DURATION_5_FIREWORK_ROCKET =
        new SimpleCraftingRecipeSerializer<>(c -> new ExtendedDurationFireworkRocketRecipe(c, true));

    private static final Supplier<Ingredient> CARD_INGREDIENT = () -> Ingredient.of(DestroyItems.CARD_STOCK.get());
    private static final Supplier<Ingredient> STAR_INGREDIENT = () -> Ingredient.of(Items.FIREWORK_STAR);
    private final Ingredient EXPLOSIVE_INGREDIENT;

    private final boolean secondaryExplosive;

    public ExtendedDurationFireworkRocketRecipe(CraftingBookCategory category, boolean secondaryExplosive) {
        super(category);
        this.secondaryExplosive = secondaryExplosive;
        this.EXPLOSIVE_INGREDIENT = explosiveIngredient(secondaryExplosive);
    }

    public static Ingredient explosiveIngredient(boolean secondaryExplosive) {
        return Ingredient.of(secondaryExplosive
            ? DestroyTags.Items.SECONDARY_EXPLOSIVES.tag
            : DestroyTags.Items.PRIMARY_EXPLOSIVES.tag);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasCardStock = false;
        boolean hasExplosive = false;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (CARD_INGREDIENT.get().test(stack)) {
                if (hasCardStock) return false;
                hasCardStock = true;
            } else if (EXPLOSIVE_INGREDIENT.test(stack)) {
                if (hasExplosive) return false;
                hasExplosive = true;
            } else if (!STAR_INGREDIENT.get().test(stack) && !stack.isEmpty()) {
                return false;
            }
            if (hasExplosive && hasCardStock) return true;
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, 10);
        List<FireworkExplosion> explosions = new ArrayList<>();

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (STAR_INGREDIENT.get().test(stack)) {
                FireworkExplosion explosion = stack.get(DataComponents.FIREWORK_EXPLOSION);
                if (explosion != null) explosions.add(explosion);
            }
        }

        int duration = secondaryExplosive ? 5 : 4;
        fireworkStack.set(DataComponents.FIREWORKS, new Fireworks(duration, explosions));
        return fireworkStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return secondaryExplosive ? DURATION_5_FIREWORK_ROCKET : DURATION_4_FIREWORK_ROCKET;
    }

    /** JEI / Recipe Book example shapeless recipes (one per duration variant).*/
    public static List<CraftingRecipe> exampleRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>(2);
        for (boolean secondary : Iterate.trueAndFalse) {
            ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET, 10);
            int duration = secondary ? 5 : 4;
            fireworkStack.set(DataComponents.FIREWORKS, new Fireworks(duration, List.of()));
            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
                CARD_INGREDIENT.get(), explosiveIngredient(secondary));
            recipes.add(new ShapelessRecipe(
                "destroy.firework.duration_" + duration,
                CraftingBookCategory.MISC,
                fireworkStack,
                inputs));
        }
        return recipes;
    }
}
