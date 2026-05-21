package petrolpark.mc.destroy.content.processing.trypolithography.keypunch;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;

import petrolpark.mc.destroy.client.DestroyPartials;

/**
 * Flywheel visual for the Keypunch — draws the selected-position piston (TransformedInstance) in
 * addition to the encased cogwheel from {@link EncasedCogVisual}. When Flywheel visualization is
 * enabled on the client, this instance-based path replaces {@link KeypunchRenderer}'s cutout/solid
 * buffer draw.
*/
public class KeypunchVisual extends EncasedCogVisual implements SimpleDynamicVisual {

    final TransformedInstance piston;
    int lastPistonPos;
    float lastPistonOffset;

    public KeypunchVisual(VisualizationContext ctx, KeypunchBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, false, partialTick, Models.partial(AllPartialModels.SHAFTLESS_COGWHEEL));
        piston = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(DestroyPartials.KEYPUNCH_PISTON))
            .createInstance();

        lastPistonPos = -1000;
        lastPistonOffset = -1000.f;
        updateAnimation(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        updateAnimation(ctx.partialTick());
    }

    /** without this it renders pure black.*/
    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(piston);
    }

    @Override
    public void _delete() {
        super._delete();
        piston.delete();
    }

    private void updateAnimation(float pt) {
        KeypunchBlockEntity be = (KeypunchBlockEntity)blockEntity;
        CircuitPunchingBehaviour behaviour = be.punchingBehaviour;

        float renderedHeadOffset = behaviour.getRenderedPistonOffset(pt);
        int pistonPos = be.getActualPosition();

        if (lastPistonPos != pistonPos || lastPistonOffset != renderedHeadOffset) {
            lastPistonPos = pistonPos;
            lastPistonOffset = renderedHeadOffset;

            piston.setIdentityTransform()
                .translate(getVisualPosition())
                .translate((4 + 2 * (pistonPos % 4)) / 16f, -(6.1f + (renderedHeadOffset * 12.5f)) / 16f, (4 + 2 * (pistonPos / 4)) / 16f)
                .setChanged();
        }
    }
}
