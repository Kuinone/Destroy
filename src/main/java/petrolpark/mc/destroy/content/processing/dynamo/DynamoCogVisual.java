package petrolpark.mc.destroy.content.processing.dynamo;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;

import petrolpark.mc.destroy.client.DestroyPartials;

/**
 * Flywheel visual for the Dynamo — instance-renders the spinning shaft as a
 * {@link SingleAxisRotatingVisual}. When Flywheel visualization is enabled, this replaces the
 * shaft render path of {@link DynamoRenderer#getRotatedModel}. The partial model swaps between
 * the regular Dynamo shaft and the Arc Furnace shaft based on {@link DynamoBlock#ARC_FURNACE}
 * blockstate at construction time.
*/
public class DynamoCogVisual extends SingleAxisRotatingVisual<KineticBlockEntity> {

    public DynamoCogVisual(VisualizationContext ctx, KineticBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick,
            Models.partial(blockEntity.getBlockState().getValue(DynamoBlock.ARC_FURNACE)
                ? DestroyPartials.ARC_FURNACE_SHAFT
                : DestroyPartials.DYNAMO_SHAFT));
    }
}
