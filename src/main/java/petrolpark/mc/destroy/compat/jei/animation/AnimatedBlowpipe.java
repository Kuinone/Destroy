package petrolpark.mc.destroy.compat.jei.animation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import dev.engine_room.flywheel.lib.transform.TransformStack;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.UIRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.content.processing.glassblowing.BlowpipeBlock;
import petrolpark.mc.destroy.content.processing.glassblowing.BlowpipeBlockEntityRenderer;
import petrolpark.mc.destroy.content.processing.glassblowing.GlassblowingRecipe;

/**
 * Animated 3D model of the Blowpipe for JEI recipe categories — renders the BLOWPIPE block
 * facing NORTH in an isometric 3/4 view + overlays the per-recipe glass-blob animation on top
 * via {@link BlowpipeBlockEntityRenderer#render}.
*/
public class AnimatedBlowpipe extends AnimatedKinetics {

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        // NOOP — 2-arg draw unused · use the GlassblowingRecipe-aware overload instead.
    }

    public void draw(GlassblowingRecipe recipe, FluidStack fluid, GuiGraphics graphics) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(0, 0, 200);
        ms.mulPose(Axis.XP.rotationDegrees(-37.5f));
        ms.mulPose(Axis.YP.rotationDegrees(247.5f));
        ms.scale(23f, 23f, 23f);
        ms.pushPose();

        blockElement(DestroyBlocks.BLOWPIPE.getDefaultState().setValue(BlowpipeBlock.FACING, Direction.NORTH))
            .render(graphics);
        TransformStack.of(ms)
            .rotateY(Mth.PI)
            .translate(-0.5f, -0.5f, 0f);
        ms.pushPose();
        UIRenderHelper.flipForGuiRender(ms);
        RenderSystem.disableDepthTest();
        BlowpipeBlockEntityRenderer.render(recipe, fluid,
            Math.min((AnimationTickHolder.getRenderTime() % 120f) / 100f, 1f),
            ms, graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        RenderSystem.enableDepthTest();
        ms.popPose();

        ms.popPose();
        ms.popPose();
    }
}
