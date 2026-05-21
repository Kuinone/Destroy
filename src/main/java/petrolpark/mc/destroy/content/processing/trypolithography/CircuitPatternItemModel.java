package petrolpark.mc.destroy.content.processing.trypolithography;

import java.util.function.Function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.ItemLayerModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

/**
 * A wrapper for a regular NeoForge {@link ItemLayerModel item model} that additionally references
 * a {@link ResourceLocation} of fragment textures — the 16-position punch-hole overlay texture
 * base path consumed by {@link CircuitPatternItemRenderer}. Model JSON declares
 * {@code "loader": "destroy:circuit_pattern"} + {@code "fragment_textures": "destroy:item/circuit_pattern/foo"}
 * to opt into this wrapper.
*/
public class CircuitPatternItemModel implements IUnbakedGeometry<CircuitPatternItemModel> {

    public final ResourceLocation fragmentTextureResourceLocation;
    public final IUnbakedGeometry<?> wrapped;

    public CircuitPatternItemModel(IUnbakedGeometry<?> wrapped, ResourceLocation fragmentTextureResourceLocation) {
        this.wrapped = wrapped;
        this.fragmentTextureResourceLocation = fragmentTextureResourceLocation;
    }

    @Override
    public CircuitPatternItemModel.Baked bake(IGeometryBakingContext context, ModelBaker baker,
                                              Function<Material, TextureAtlasSprite> spriteGetter,
                                              ModelState modelState, ItemOverrides overrides) {
        return new Baked(wrapped.bake(context, baker, spriteGetter, modelState, overrides));
    }

    public class Baked extends BakedModelWrapper<BakedModel> {

        public Baked(BakedModel originalModel) {
            super(originalModel);
        }

        public ResourceLocation getFragmentTextureResourceLocation() {
            return fragmentTextureResourceLocation;
        }
    }

    public static class Loader implements IGeometryLoader<CircuitPatternItemModel> {

        public static final Loader INSTANCE = new Loader();

        @Override
        public CircuitPatternItemModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
            ItemLayerModel itemModel = ItemLayerModel.Loader.INSTANCE.read(jsonObject, deserializationContext);
            if (jsonObject.has("fragment_textures")) {
                try {
                    return new CircuitPatternItemModel(itemModel,
                        ResourceLocation.parse(jsonObject.get("fragment_textures").getAsString()));
                } catch (UnsupportedOperationException | IllegalStateException e) {
                    throw new JsonParseException("Cannot read Circuit Pattern item model", e);
                }
            } else {
                throw new JsonParseException("Circuit Pattern item models must refer to a texture of fragments");
            }
        }
    }
}
