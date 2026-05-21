package petrolpark.mc.destroy.content.processing.discstamping;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyRecipeTypes;

/**
 * Disc Electroplating recipe — Basin processing variant for Dynamo-driven electroplating of
 * music discs into stamper patterns. The {@link #original} flag distinguishes the "template"
 * recipe (loaded from JSON data-pack) from the dynamically-copied per-disc recipe spawned by
 * {@link #copyWithDisc(ItemStack)} at runtime when a player drops a music disc into the Basin.
 * Each template recipe defines the fluid/item inputs + the disc-stamper output pattern;
 * {@code copyWithDisc} clones the template with the specific disc added as an extra ingredient
 * and a disc-stamper (carrying that disc in its {@code STAMPED_DISC} DataComponent) as the
 * output.
*/
public class DiscElectroplatingRecipe extends BasinRecipe {

    public final boolean original;

    public DiscElectroplatingRecipe(ProcessingRecipeParams params) {
        this(params, true);
    }

    private DiscElectroplatingRecipe(ProcessingRecipeParams params, boolean original) {
        super(DestroyRecipeTypes.DISC_ELECTROPLATING, params);
        this.original = original;
    }

    /**
 * Spawn a runtime-only electroplating recipe customized for the given music disc. The
 * template recipe's fluid/item inputs are preserved; the disc is appended as an extra
 * item ingredient, and a disc-stamper stack carrying the disc replaces the output.
 * Used by {@code DynamoBlockEntity.getMatchingRecipes} when MUSIC_DISCS-tagged items are
 * present in the Basin (runtime recipe isn't registered; marked {@link #original} =
 * false to distinguish from JSON templates).
*/
    public BasinRecipe copyWithDisc(ItemStack discStack) {
        StandardProcessingRecipe.Builder<DiscElectroplatingRecipe> builder =
            new StandardProcessingRecipe.Builder<>(
                params -> new DiscElectroplatingRecipe(params, false),
                Destroy.asResource("disc_electroplating_" + Item.getId(discStack.getItem())))
            .requiresHeat(requiredHeat)
            .duration(processingDuration)
            .require(Ingredient.of(discStack))
            .output(DiscStamperItem.of(discStack));

        // ingredients/results to the builder. S146 mistakenly switched to the
        // {@code withFluidIngredients/withItemOutputs/withFluidOutputs} "bulk helpers" thinking
        // they appended a list — they actually OVERWRITE: {@code params.results = outputs;}
        // (verified in Create 1.21 ProcessingRecipeBuilder.java:67-69). With template
        // {@code results: []} (empty), the bulk-call wiped out the {@code .output(stamped
        // disc_stamper)} added two lines above. Net: every disc_electroplating runtime recipe
        // had ZERO outputs → JEI rendered the output slot as an empty placeholder (the "gray
        // cube" the user reported, mis-diagnosed as a missing-texture / un-ported disc_stamper),
        // and Dynamo would actually fail the recipe at execute time too because there's
        // nothing to produce.
        // {@code DiscElectroplatingRecipe.copyWithDisc}.
        ingredients.forEach(builder::require);
        fluidIngredients.forEach(builder::require);
        results.forEach(builder::output);
        fluidResults.forEach(builder::output);

        return builder.build();
    }
}
