package petrolpark.mc.destroy.compat.jei.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Generic JEI helper for rendering any {@link BlockState} in an isometric 3/4 view within a
 * recipe category (used by non-kinetic blocks like Extrusion Die where no rotation animation is
 * needed). Extends Create's {@link AnimatedKinetics} only to reuse the {@code blockElement}
 * factory — {@link #draw} is a no-op since the generic renderer doesn't own a draw site.
*/
public class GUIBlockRenderer extends AnimatedKinetics {

    /**
 * Render the given {@link BlockState} at (0, 0, 0) rotated into an isometric 3/4 view.
 * Called by category {@code draw(...)} implementations within a {@code pushPose/popPose}
 * wrap plus a {@code translate(x, y, 0)} positioning step.
 *
 * @param blockState the state to render
 * @param scale relative size (typical range: 16–30 for JEI category slots)
*/
    public void renderBlock(BlockState blockState, GuiGraphics graphics, double scale) {
        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        stack.mulPose(Axis.YP.rotationDegrees(22.5f));
        blockElement(blockState)
            .atLocal(0, 0, 0)
            .scale(scale)
            .render(graphics);
        stack.popPose();
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        // No-op · GUIBlockRenderer is used as a utility helper, not a per-frame draw target.
    }
}
