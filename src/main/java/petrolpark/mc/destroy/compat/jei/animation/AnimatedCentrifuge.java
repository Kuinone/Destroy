package petrolpark.mc.destroy.compat.jei.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import net.minecraft.client.gui.GuiGraphics;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.client.DestroyPartials;

/**
 * Animated 3D model of the Centrifuge for JEI recipe categories — renders the CENTRIFUGE block
 * + spinning CENTRIFUGE_COG inner partial, rotated into an isometric 3/4 view.
*/
public class AnimatedCentrifuge extends AnimatedKinetics {

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(112.5f));
        int scale = 23;

        blockElement(DestroyPartials.CENTRIFUGE_COG)
            .rotateBlock(0, getCurrentAngle() * 2, 0)
            .atLocal(0, 0, 0)
            .scale(scale)
            .render(graphics);

        blockElement(DestroyBlocks.CENTRIFUGE.getDefaultState())
            .atLocal(0, 0, 0)
            .scale(scale)
            .render(graphics);

        matrixStack.popPose();
    }
}
