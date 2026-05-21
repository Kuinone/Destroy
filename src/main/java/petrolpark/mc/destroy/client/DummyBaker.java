package petrolpark.mc.destroy.client;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;

/**
 * Minimal {@link ModelBaker} implementation used to bake item-fragment models on-the-fly —
 * delegates to {@link UnbakedGeometryHelper#bake} for actual model baking while returning null/stub
 * values for the rest of the baker interface (no resource-manager reload, no upstream baking
 * pipeline participation).
*/
public class DummyBaker implements ModelBaker {

    public static final DummyBaker BAKER = new DummyBaker();

    /**
 * Bake a BlockModel using this dummy baker, delegating to {@link UnbakedGeometryHelper#bake}.*/
    public static BakedModel bake(BlockModel model, ResourceLocation location) {
        return bake(model, model, location);
    }

    public static BakedModel bake(BlockModel model, BlockModel modelOwner, ResourceLocation location) {
        // 1.21 signature: (model, baker, modelOwner, spriteGetter, modelState, guiLight)
        return UnbakedGeometryHelper.bake(model, BAKER, model, Material::sprite, BlockModelRotation.X0_Y0, false);
    }

    // --- ModelBaker interface stubs ---

    @Override
    public UnbakedModel getModel(ResourceLocation pLocation) {
        return null;
    }

    @Override
    public BakedModel bake(ResourceLocation location, ModelState state) {
        return bake(location, state, getModelTextureGetter());
    }

    // --- IModelBakerExtension (NeoForge 1.21) ---

    @Override
    public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
        return Material::sprite;
    }

    @Override
    public @Nullable BakedModel bake(ResourceLocation location, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
        return null;
    }

    @Override
    public @Nullable UnbakedModel getTopLevelModel(ModelResourceLocation location) {
        return null;
    }

    @Override
    public @Nullable BakedModel bakeUncached(UnbakedModel unbakedModel, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
        return null;
    }
}
