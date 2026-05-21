package petrolpark.mc.destroy.core.explosion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Renders a {@link PrimedBombEntity} as its {@link PrimedBombEntity#getBlockStateToRender()} with
 * the vanilla TNT countdown pulse + grow-on-final-seconds animation. Generic {@code <T>} supports
 * one renderer for all 4 bomb subtypes.
*/
public class PrimedBombEntityRenderer<T extends PrimedBombEntity> extends EntityRenderer<T> {

    protected final BlockRenderDispatcher blockRenderer;

    public PrimedBombEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int packedLight) {
        ms.pushPose();
        ms.translate(0.0F, 0.5F, 0.0F);
        int fuse = entity.getFuse();
        if ((float) fuse - partialTicks < 9f) {
            // Grow the entity as it is about to explode
            float scale = 1f + (float) Math.pow(Mth.clamp(1f - ((float) fuse - partialTicks + 1f) / 10f, 0f, 1f), 3) * 0.3f;
            ms.scale(scale, scale, scale);
        }
        ms.mulPose(Axis.YP.rotationDegrees(-90f));
        ms.translate(-0.5f, -0.5f, 0.5f);
        ms.mulPose(Axis.YP.rotationDegrees(90f));
        renderBlock(entity, ms, buffer, packedLight, fuse);
        ms.popPose();
    }

    public void renderBlock(T entity, PoseStack ms, MultiBufferSource buffer, int light, int fuse) {
        TntMinecartRenderer.renderWhiteSolidBlock(blockRenderer, entity.getBlockStateToRender(),
            ms, buffer, light, fuse / 5 % 2 == 0);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResourceLocation getTextureLocation(T entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
