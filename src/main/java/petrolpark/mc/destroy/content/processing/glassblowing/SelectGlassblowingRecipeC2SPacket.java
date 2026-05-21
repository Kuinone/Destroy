package petrolpark.mc.destroy.content.processing.glassblowing;

import com.simibubi.create.foundation.recipe.RecipeFinder;

import io.netty.buffer.ByteBuf;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.DestroyPackets;

/**
 * Client → server: select a {@link GlassblowingRecipe} for the Blowpipe in the given hand. Sent
 * from {@link BlowpipeScreen} when the player picks a recipe button. The server writes the chosen
 * recipe ID and required-fluid DataComponents onto the Blowpipe stack, resets blowing progress,
 * and voids the internal tank if its current fluid no longer matches the new recipe's
 * ingredient.
*/
public record SelectGlassblowingRecipeC2SPacket(InteractionHand hand, ResourceLocation recipeId)
    implements ServerboundPacketPayload {

    private static final Object recipeCacheKey = new Object();

    public static final StreamCodec<ByteBuf, SelectGlassblowingRecipeC2SPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, p -> p.hand() == InteractionHand.MAIN_HAND,
            ResourceLocation.STREAM_CODEC, SelectGlassblowingRecipeC2SPacket::recipeId,
            (mainHand, id) -> new SelectGlassblowingRecipeC2SPacket(
                mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, id));

    @Override
    public BasePacketPayload.PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SELECT_GLASSBLOWING_RECIPE;
    }

    @Override
    public void handle(ServerPlayer player) {
        ItemStack stack = player.getItemInHand(hand);

        // unwrap via rh.value() + rh.id().
        GlassblowingRecipe recipe = RecipeFinder
            .get(recipeCacheKey, player.level(), rh -> rh.value() instanceof GlassblowingRecipe)
            .stream()
            .filter(rh -> rh.id().equals(recipeId))
            .map(rh -> (GlassblowingRecipe) rh.value())
            .findFirst()
            .orElse(null);
        if (recipe == null) return;

        ResourceLocation existingRecipe = stack.get(DestroyDataComponents.BLOWPIPE_RECIPE);
        if (existingRecipe == null) {
            // Fresh selection — no existing state to reset.
        } else {
            if (existingRecipe.equals(recipeId)) return; // idempotent no-op for same-recipe reselect
            // Tank fluid must still satisfy new recipe's ingredient; void if not.
            FluidStack tankFluid = stack.getOrDefault(DestroyDataComponents.BLOWPIPE_TANK, FluidStack.EMPTY);
            if (!tankFluid.isEmpty()) {
                SizedFluidIngredient sized = recipe.getFluidIngredients().get(0);
                // SizedFluidIngredient.test
                // semantics may or may not include amount — use explicit split for safety.
                if (!sized.ingredient().test(tankFluid) || tankFluid.getAmount() < sized.amount()) {
                    stack.set(DestroyDataComponents.BLOWPIPE_TANK, FluidStack.EMPTY);
                }
            }
            stack.set(DestroyDataComponents.BLOWPIPE_PROGRESS, 0);
            stack.set(DestroyDataComponents.BLOWPIPE_LAST_PROGRESS, 0);
        }

        stack.set(DestroyDataComponents.BLOWPIPE_RECIPE, recipeId);
        stack.set(DestroyDataComponents.BLOWPIPE_REQUIRED_FLUID, recipe.getFluidIngredients().get(0));
    }
}
