package petrolpark.mc.destroy.core.block.copycat;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.decoration.copycat.CopycatModel;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Baked-model variant of Create's {@link CopycatModel} that fully copies the wrapped material's
 * geometry — i.e. a vat-side cell with iron-block material renders as a complete iron block,
 * not Create's default panel/step shape.
*/
public class CopycatFullBlockModel extends CopycatModel {

    public CopycatFullBlockModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    protected List<BakedQuad> getCroppedQuads(BlockState state, Direction side, RandomSource rand,
                                              BlockState material, ModelData wrappedData,
                                              RenderType renderType) {
        // Full-block copy: forward the wrapped material model's quads verbatim (cloned so callers
        // can't mutate the wrapped model's quad list).
        return getModelOf(material).getQuads(material, side, rand, wrappedData, renderType)
            .stream()
            .map(BakedQuadHelper::clone)
            .toList();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
                                             @NotNull ModelData data) {
        // The wrapped material
        // BakedModel already advertises its own render types via gatherModelData / getRenderTypes.
        return originalModel.getRenderTypes(state, rand, data);
    }
}
