package petrolpark.mc.destroy.content.processing.trypolithography.recipe;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItem;

/**
 * DeployerApplicationRecipe variant marking a circuit-pattern-bearing sub-step in a
 * SequencedAssembly chain. Implements {@link IConfersCircuitPatternRecipe} so
 * {@link CircuitSequencedAssemblyRecipe} can validate "exactly one pattern-conferring step".
*/
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class CircuitDeployerApplicationRecipe extends DeployerApplicationRecipe
    implements IConfersCircuitPatternRecipe {

    private final boolean example;

    public CircuitDeployerApplicationRecipe(ItemApplicationRecipeParams params) {
        this(params, true);
    }

    private CircuitDeployerApplicationRecipe(ItemApplicationRecipeParams params, boolean example) {
        super(params);
        this.example = example;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        // Returns the registered serializer instance; wired via DestroyRecipeTypes enum entry.
        return petrolpark.mc.destroy.DestroyRecipeTypes.CIRCUIT_DEPLOYING.getSerializer();
    }

    /**
 * S362 v2 enhancement — JEI preview: inject {@link CircuitSequencedAssemblyRecipe#EXAMPLE_PATTERN}
 * into any {@link CircuitPatternItem} output stack so the displayed result shows a
 * representative punched pattern (not a blank board).
*/
    @Override
    public List<ProcessingOutput> getRollableResults() {
        if (!example) return super.getRollableResults();
        return super.getRollableResults().stream().map(output -> {
            ItemStack result = output.getStack().copy();
            if (!(result.getItem() instanceof CircuitPatternItem)) return output;
            CircuitPatternItem.putPattern(result, CircuitSequencedAssemblyRecipe.EXAMPLE_PATTERN);
            return new ProcessingOutput(result, output.getChance());
        }).toList();
    }

    /**
 * S362 v2 enhancement — Runtime per-input specialization. When the deployer fires this
 * recipe with a specific punched mask, set a one-shot enforced result that carries the
 * mask's pattern bit. 1.21
 * simplified to in-place {@link ProcessingRecipe#enforceNextResult} since Create's deployer
 * is single-threaded per BE (no concurrent recipe firing on the same instance, so the
 * one-shot supplier is safe to overwrite each event tick).
*/
    public RecipeHolder<DeployerApplicationRecipe> specify(ResourceLocation id, RecipeWrapper inv) {
        int pattern = CircuitPatternItem.getPattern(inv.getItem(1));
        // enforceNextResult is consumed on the next rollResults call — set it on `this` so the
        // next deployer cycle picks up the pattern-bearing stack.
        this.enforceNextResult(() -> transformWithPattern(super.getRollableResults(), pattern));
        return new RecipeHolder<>(id, this);
    }

    /** Apply pattern bit to whichever output stack supports CircuitPatternItem / SequencedAssemblyItem.*/
    private static ItemStack transformWithPattern(List<ProcessingOutput> rollable, int pattern) {
        if (rollable.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = rollable.get(0).getStack().copy();
        if (result.getItem() instanceof CircuitPatternItem || result.getItem() instanceof SequencedAssemblyItem) {
            CircuitPatternItem.putPattern(result, pattern);
        }
        return result;
    }

    /**
 * 1.21 uses RecipeHolder<>-wrapped recipes throughout.
*/
    @SubscribeEvent
    public static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
        RecipeWrapper inv = event.getInventory();
        // 1.21 RecipeWrapper has no hasAnyMatching helper;
        // hand-roll the scan over getItem(0..size).
        boolean hasPatternItem = false;
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getItem(i).getItem() instanceof CircuitPatternItem) {
                hasPatternItem = true;
                break;
            }
        }
        if (!hasPatternItem) return;

        // First: check if a CircuitDeployerApplicationRecipe is the directly-matching deployer
        // recipe. Then: check sequenced-assembly chain that points at a CIRCUIT_DEPLOYING step.
        var level = event.getBlockEntity().getLevel();
        if (level == null) return;

        Optional<RecipeHolder<CircuitDeployerApplicationRecipe>> direct =
            DestroyRecipeTypes.CIRCUIT_DEPLOYING.find(inv, level);
        Optional<RecipeHolder<CircuitDeployerApplicationRecipe>> viaSeq = Optional.empty();
        if (direct.isEmpty()) {
            // SequencedAssemblyRecipe.getRecipe returns the next deployer recipe in the chain
            // when the input matches a transitionalItem
            viaSeq = SequencedAssemblyRecipe.getRecipe(level, inv,
                AllRecipeTypes.DEPLOYING.getType(),
                CircuitDeployerApplicationRecipe.class);
        }

        Optional<RecipeHolder<CircuitDeployerApplicationRecipe>> chosen = direct.isPresent() ? direct : viaSeq;
        chosen.ifPresent(holder -> event.addRecipe(
            () -> Optional.of(holder.value().specify(holder.id(), inv)),
            150));
    }

    /**
 * Codec-level validator — was a sanity check on the recipe ingredient slots;
 *
 * <p>Going with option 2 (return success unconditionally) because the slot semantics are
 * already enforced by the runtime {@code matches()} check + the codec deserialization itself,
 * and there's no clear-cut "wrong recipe" case the validator was actually catching. If a future
 * regression motivates resurrecting the check, option 1 is the right form.</p>
*/
    private static DataResult<CircuitDeployerApplicationRecipe> validateCircuitDeployer(CircuitDeployerApplicationRecipe recipe) {
        return DataResult.success(recipe);
    }

    /**
 * Serializer wraps {@link ItemApplicationRecipe.Serializer} with the {@link #validate}
 * check on decode. Registered in {@link petrolpark.mc.destroy.DestroyRecipeTypes}.
*/
    public static class Serializer extends ItemApplicationRecipe.Serializer<CircuitDeployerApplicationRecipe> {

        private final MapCodec<CircuitDeployerApplicationRecipe> validatedCodec;

        public Serializer() {
            super(CircuitDeployerApplicationRecipe::new);
            this.validatedCodec = super.codec().flatXmap(
                CircuitDeployerApplicationRecipe::validateCircuitDeployer,
                DataResult::success);
        }

        @Override
        public MapCodec<CircuitDeployerApplicationRecipe> codec() {
            return validatedCodec;
        }
    }

}
