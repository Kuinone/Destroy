package petrolpark.mc.destroy.content.processing.sieve;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import petrolpark.mc.destroy.client.DestroyPartials;

/**
 * Flywheel visual for the Mechanical Sieve — draws the sieve-pan and linkages as
 * TransformedInstances that animate in sync with the shaft rotation via sine (pan oscillation)
 * and z-rotation (linkages spin). When Flywheel visualization is enabled, these instances render
 * in place of {@link MechanicalSieveRenderer}'s cutout/solid buffer draws.
*/
public class MechanicalSieveVisual extends SingleAxisRotatingVisual<MechanicalSieveBlockEntity> implements SimpleDynamicVisual {

    protected final TransformedInstance sieve;
    protected final TransformedInstance linkages;

    public MechanicalSieveVisual(VisualizationContext ctx, MechanicalSieveBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick, Models.partial(DestroyPartials.MECHANICAL_SIEVE_SHAFT));

        sieve = instancerProvider()
            .instancer(InstanceTypes.TRANSFORMED, Models.partial(DestroyPartials.MECHANICAL_SIEVE))
            .createInstance();

        linkages = instancerProvider()
            .instancer(InstanceTypes.TRANSFORMED, Models.partial(DestroyPartials.MECHANICAL_SIEVE_LINKAGES))
            .createInstance();

        updateAnimation();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        updateAnimation();
    }

    /** without this they render pure black.*/
    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(sieve, linkages);
    }

    @Override
    public void _delete() {
        super._delete();
        sieve.delete();
        linkages.delete();
    }

    private void updateAnimation() {
        Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
        float angle = KineticBlockEntityRenderer.getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis);
        Direction facing = blockState.getValue(MechanicalSieveBlock.X) ? Direction.EAST : Direction.SOUTH;

        float offset = (float) (Mth.sin(angle) * 2 / 16d);

        sieve.setIdentityTransform()
            .translate(getVisualPosition())
            .center()
            .rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .uncenter()
            .translate(offset, 0d, 0d)
            .setChanged();

        linkages.setIdentityTransform()
            .translate(getVisualPosition())
            .center()
            .rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .translate(offset, 0d, 0d)
            .rotateZ(-angle)
            .uncenter()
            .setChanged();
    }
}
