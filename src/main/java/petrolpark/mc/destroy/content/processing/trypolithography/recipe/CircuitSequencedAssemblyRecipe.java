package petrolpark.mc.destroy.content.processing.trypolithography.recipe;

import java.util.stream.Stream;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeSerializer;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItem;

/**
 * SequencedAssemblyRecipe variant that propagates a circuit pattern from the input
 * {@link CircuitPatternItem} (or a sub-{@link IConfersCircuitPatternRecipe} step) to the result.
 * Adds JSON-time validation: the recipe MUST have exactly one mechanism that determines the
 * resulting pattern (input item OR sub-recipe), otherwise the result would carry an undefined
 * pattern.
*/
public class CircuitSequencedAssemblyRecipe extends SequencedAssemblyRecipe {

    /**
 * 4×4 example pattern shown in JEI / GUI when previewing a Circuit Sequenced Assembly recipe
 * result before any real Punch step has run.
*/
    public static final int EXAMPLE_PATTERN = 0b1000001000010100;

    public CircuitSequencedAssemblyRecipe(SequencedAssemblyRecipeSerializer serializer) {
        super(serializer);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack stack = resultPool.get(0).getStack().copy();
        CircuitPatternItem.putPattern(stack, EXAMPLE_PATTERN);
        return stack;
    }

    /**
 * Codec-level validator for the assembled SequencedAssemblyRecipe — re-applied as a wrapper
 * around the parent serializer's codec. Returns {@link DataResult#error} with a descriptive
 * message when the recipe has 0 or 2+ pattern-conferring mechanisms.
*/
    private static DataResult<SequencedAssemblyRecipe> validate(SequencedAssemblyRecipe recipe) {
        boolean inputHasPattern = !Stream.of(recipe.getIngredient().getItems())
            .anyMatch(stack -> !(stack.getItem() instanceof CircuitPatternItem));
        if (!inputHasPattern && recipe.getLoops() != 1) {
            return DataResult.error(() -> "Circuit Sequenced Assembly recipes may not loop "
                + "(unless the Circuit Pattern is determined by the input item), as it makes "
                + "ambiguous the step at which the Circuit Pattern of the result is determined.");
        }
        boolean foundConferringRecipe = inputHasPattern;
        for (SequencedRecipe<?> subRecipe : recipe.getSequence()) {
            if (subRecipe.getRecipe() instanceof IConfersCircuitPatternRecipe) {
                if (foundConferringRecipe) {
                    return DataResult.error(() -> "Circuit Sequenced Assembly recipes may only "
                        + "define a single sub-recipe that can determine the Circuit Pattern "
                        + "of the result.");
                }
                foundConferringRecipe = true;
            }
        }
        if (!foundConferringRecipe) {
            return DataResult.error(() -> "Circuit Sequenced Assembly recipes must define a "
                + "step in which the Circuit Pattern of the result gets determined.");
        }
        return DataResult.success(recipe);
    }

    /**
 * Serializer wrapping the parent's codec with the {@link #validate} check. Decode goes
 * through validation; encode delegates straight to parent.
*/
    public static class Serializer extends SequencedAssemblyRecipeSerializer {

        private final MapCodec<SequencedAssemblyRecipe> validatedCodec =
            super.codec().flatXmap(
                CircuitSequencedAssemblyRecipe::validate,
                DataResult::success);

        @Override
        public MapCodec<SequencedAssemblyRecipe> codec() {
            return validatedCodec;
        }

        // streamCodec preserved from parent (network-side validation skipped — server already
        // ran codec-decode and rejected invalid recipes; client trusts server payload).
    }
}
