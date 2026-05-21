package petrolpark.mc.destroy.content.processing.centrifuge;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.client.DestroyPartials;

/**
 * Block-Entity Renderer for the Centrifuge — overrides {@link #getRotatedModel} so the kinetic
 * shaft visualization renders the Centrifuge's inner cog ({@link DestroyPartials#CENTRIFUGE_COG})
 * instead of the default Create shaft. Trivial Renderer — no custom
 * renderSafe logic, the cog spin is driven entirely by the {@link KineticBlockEntityRenderer}
 * base class's kinetic-speed rotation.
*/
public class CentrifugeRenderer extends KineticBlockEntityRenderer<CentrifugeBlockEntity> {

    public CentrifugeRenderer(Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(CentrifugeBlockEntity be, BlockState state) {
        return CachedBuffers.partial(DestroyPartials.CENTRIFUGE_COG, state);
    }

}
