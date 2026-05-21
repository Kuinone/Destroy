package petrolpark.mc.destroy.content.processing.glassblowing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;

import dev.engine_room.flywheel.lib.transform.TransformStack;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.DestroyDataComponents;

/**
 * First-person / inventory-GUI Blowpipe renderer — draws the blown-glass blob hovering off the
 * tip of the pipe matching the selected {@link GlassblowingRecipe}. In first-person hand
 * context the pipe translates toward the player's mouth during the
 * {@link BlowpipeItem#TIME_TO_MOVE_TO_MOUTH} windup; third-person is handled by
 * {@link BlowpipeItemRenderLayer} instead. In the GUI context (shift-held), draws the recipe
 * result at small scale in the corner as a "preview of finished product".
*/
public class BlowpipeItemRenderer extends CustomRenderedItemModelRenderer {

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                          ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer,
                          int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        float partialTicks = AnimationTickHolder.getPartialTicks();

        boolean blowing = stack.getOrDefault(DestroyDataComponents.BLOWPIPE_BLOWING, false);

        ms.pushPose();
        if (blowing) {
            if (transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                ms.popPose();
                return; // Handled by BlowpipeItemRenderLayer in third-person
            } else if (transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                float movementProgress;
                int ticks = mc.player.getTicksUsingItem();
                if (ticks < BlowpipeItem.TIME_TO_MOVE_TO_MOUTH) {
                    movementProgress = ((float) ticks + partialTicks) / BlowpipeItem.TIME_TO_MOVE_TO_MOUTH;
                    movementProgress = 3 * movementProgress * movementProgress - 2 * movementProgress * movementProgress * movementProgress;
                } else {
                    movementProgress = 1f;
                }
                ms.translate(-9 / 16f * movementProgress, 3 / 16f * movementProgress, 0f);
            }
        }
        itemRenderer.render(stack, ItemDisplayContext.NONE, false, ms, buffer, light, overlay, model.getOriginalModel());

        GlassblowingRecipe recipe = BlowpipeBlockEntity.readRecipeFromStack(mc.level, stack);
        FluidStack tankFluid = stack.getOrDefault(DestroyDataComponents.BLOWPIPE_TANK, FluidStack.EMPTY);
        if (recipe == null) {
            ms.popPose();
            return;
        }

        int lastProgress = stack.getOrDefault(DestroyDataComponents.BLOWPIPE_LAST_PROGRESS, 0);
        int progress = stack.getOrDefault(DestroyDataComponents.BLOWPIPE_PROGRESS, 0);
        float progressProportion = (float) lastProgress / BlowpipeBlockEntity.BLOWING_DURATION;
        if (progress != lastProgress) progressProportion += partialTicks / BlowpipeBlockEntity.BLOWING_DURATION;

        if (transformType == ItemDisplayContext.GUI) {
            if (Screen.hasShiftDown()) {
                ms.popPose();
                ms.popPose();
                ms.popPose();
                ms.translate(-1 / 4f, -1 / 4f, 1);
                ms.scale(0.5f, 0.5f, 0.5f);
                itemRenderer.renderStatic(recipe.getRollableResults().get(0).getStack(),
                    ItemDisplayContext.GUI, light, OverlayTexture.NO_OVERLAY, ms, buffer, mc.level, 0);
                ms.pushPose();
                ms.pushPose();
                ms.pushPose();
            }
        } else if (!tankFluid.isEmpty()) {
            // rotateY(180)`) after fixed the underlying 16× shape-size bug that was
            // making the i+1 "fix" (`translate(0, 0.5, 0) + rotateX(-90)`) look correct in
            // With the upstream's correct shape sizing restored, the i+0 transform
            // is the right one — the i+1 changes appear to have been compensating for a
            // size bug the upstream itself had introduced and never properly fixed.
            // After this transform: origin sits ~0.5 along the pipe's long axis (mouth→tip
            // direction); the 180° Y-rotation flips the BER's native +Z draw axis to point
            // along the held pipe forward, so glass extends from mouth along the rod toward
            // the tip — matching the held-blowpipe glassblowing intuition.
            ms.pushPose();
            ms.translate(0f, 0f, -8 / 16f);
            TransformStack.of(ms).rotateYDegrees(180);
            BlowpipeBlockEntityRenderer.render(recipe, tankFluid, progressProportion, ms, buffer, light, overlay);
            ms.popPose();
        }

        ms.popPose();
    }
}
