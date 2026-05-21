package petrolpark.mc.destroy.compat.jei.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import net.minecraft.client.gui.GuiGraphics;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.client.DestroyPartials;
import petrolpark.mc.destroy.content.processing.dynamo.DynamoBlock;

/**
 * Animated 3D model of the Dynamo for JEI recipe categories — renders the DYNAMO block with
 * spinning DYNAMO_SHAFT partial + optional ARC_FURNACE_LID on top + optional BASIN below
 * (depending on recipe category: BELT charging / arc furnace / basin electrolysis).
*/
public class AnimatedDynamo extends AnimatedKinetics {

    private final boolean basin;
    private final boolean arcFurnace;

    public AnimatedDynamo(boolean basin, boolean arcFurnace) {
        this.basin = basin;
        this.arcFurnace = arcFurnace;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        blockElement(arcFurnace ? DestroyPartials.ARC_FURNACE_SHAFT : DestroyPartials.DYNAMO_SHAFT)
            .rotateBlock(0, getCurrentAngle() * 2, 0)
            .atLocal(0, 0, 0)
            .scale(scale)
            .render(graphics);

        blockElement(DestroyBlocks.DYNAMO.getDefaultState().setValue(DynamoBlock.ARC_FURNACE, arcFurnace))
            .atLocal(0, 0, 0)
            .scale(scale)
            .render(graphics);

        if (arcFurnace) {
            blockElement(DestroyBlocks.ARC_FURNACE_LID.getDefaultState())
                .atLocal(0d, 0.825d, 0d)
                .scale(scale)
                .render(graphics);
        }

        if (basin) {
            blockElement(AllBlocks.BASIN.getDefaultState())
                .atLocal(0, 1.65, 0)
                .scale(scale)
                .render(graphics);
        }

        matrixStack.popPose();
    }
}
