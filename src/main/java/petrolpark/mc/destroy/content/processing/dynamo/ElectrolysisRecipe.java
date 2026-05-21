package petrolpark.mc.destroy.content.processing.dynamo;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import petrolpark.mc.destroy.DestroyRecipeTypes;

/**
 * Electrolysis recipe — Basin processing that a Dynamo drives to split fluid/item mixtures
 * via electrical current. Thin {@link BasinRecipe} subclass; all ingredient/output wiring
 * comes from {@code BasinRecipe}'s JSON-Codec deserialization.
*/
public class ElectrolysisRecipe extends BasinRecipe {

    public ElectrolysisRecipe(ProcessingRecipeParams params) {
        super(DestroyRecipeTypes.ELECTROLYSIS, params);
        if (processingDuration == 0) processingDuration = 200;
    }
}
