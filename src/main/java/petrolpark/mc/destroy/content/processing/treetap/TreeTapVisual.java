package petrolpark.mc.destroy.content.processing.treetap;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import petrolpark.mc.destroy.client.DestroyPartials;

/**
 * Flywheel visual for the Tree Tap — draws the animated arm as a TransformedInstance that's
 * dynamically re-posed each frame based on kinetic shaft rotation. When Flywheel visualization
 * is enabled, this instance renders in place of {@link TreeTapRenderer}.
*/
public class TreeTapVisual extends ShaftVisual<TreeTapBlockEntity> implements SimpleDynamicVisual {

    protected final TransformedInstance arm;

    public TreeTapVisual(VisualizationContext ctx, TreeTapBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        arm = ctx.instancerProvider()
            .instancer(InstanceTypes.TRANSFORMED, Models.partial(DestroyPartials.TREE_TAP_ARM))
            .createInstance();

        updateAnimation();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        updateAnimation();
    }

    /** Without this override the {@code arm}
 * TransformedInstance has packed light = 0 (= pure black). The 1.21 port docstring above
 * **misread** the Flywheel 1.21 API: `updateLight` is NOT removed — it's
 * still on `AbstractBlockEntityVisual` and required for any extra Instance fields the
 * visual adds beyond the parent's `rotatingModel`. The parent {@code SingleAxisRotatingVisual.
 * updateLight} only relights its own `rotatingModel`; we must override + super-call + relight
 * the arm too. Same pattern is in PumpjackVisual (already correct).
*/
    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(arm);
    }

    @Override
    public void _delete() {
        super._delete();
        arm.delete();
    }

    private void updateAnimation() {
        Direction facing = blockState.getValue(TreeTapBlock.FACING);

        arm.setIdentityTransform()
            .translate(getVisualPosition())
            .center()
            .rotateDegrees(9f * Mth.sin(KineticBlockEntityRenderer.getAngleForBe(blockEntity, blockEntity.getBlockPos(), facing.getClockWise().getAxis())),
                facing.getClockWise().getAxis())
            .rotateToFace(facing.getOpposite())
            .uncenter()
            .setChanged();
    }
}
