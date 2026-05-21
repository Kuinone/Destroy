package petrolpark.mc.destroy.content.processing.discstamping;

import java.util.Optional;

import javax.annotation.Nullable;

import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.DestroyItems;

/**
 * Deployer-application recipe — when a Deployer holds a {@link DiscStamperItem} with a stamped
 * disc and presses a {@code BLANK_MUSIC_DISC}, output the stamped disc (copy of the stamper's
 * {@code STAMPED_DISC} DataComponent). The stamper is not consumed (reusable tool).
 *
 * <p>Runtime-generated recipe: this class does NOT register a RecipeType — instead it hooks
 * {@link DeployerRecipeSearchEvent} and builds a recipe on-the-fly when Deployer inventory
 * matches {@code (blank disc, disc stamper with non-empty STAMPED_DISC)}.</p>
*/
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class DiscStampingRecipe extends DeployerApplicationRecipe {

    public DiscStampingRecipe(ItemApplicationRecipeParams params) {
        super(params);
    }

    /**
 * Runtime factory — produces a DeployerApplicationRecipe for a specific stamper stack.
 * Returns null if the stamper has no stamped disc (can't produce an output).
*/
    @Nullable
    public static DeployerApplicationRecipe create(ItemStack discStamper) {
        if (!(discStamper.getItem() instanceof DiscStamperItem)) return null;
        ItemStack disc = DiscStamperItem.getDisc(discStamper);
        if (disc.isEmpty()) return null;
        return new ItemApplicationRecipe.Builder<DiscStampingRecipe>(
                DiscStampingRecipe::new,
                Destroy.asResource("disc_stamping_" + Item.getId(disc.getItem())))
            .require(Ingredient.of(DestroyItems.BLANK_MUSIC_DISC))
            .require(DataComponentIngredient.of(true, discStamper))
            .output(disc)
            .toolNotConsumed()
            .build();
    }

    @Override
    public boolean supportsAssembly() {
        return false;
    }

    
    @SubscribeEvent
    public static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
        RecipeWrapper inv = event.getInventory();
        ItemStack appliedStack = inv.getItem(1);
        if (appliedStack.getItem() instanceof DiscStamperItem
            && inv.getItem(0).is(DestroyItems.BLANK_MUSIC_DISC.get())) {
            event.addRecipe(() -> {
                DeployerApplicationRecipe recipe = DiscStampingRecipe.create(appliedStack);
                if (recipe == null) return Optional.empty();
                ItemStack disc = DiscStamperItem.getDisc(appliedStack);
                ResourceLocation id = Destroy.asResource("disc_stamping_" + Item.getId(disc.getItem()));
                return Optional.of(new RecipeHolder<>(id, recipe));
            }, 75);
        }
    }

}
