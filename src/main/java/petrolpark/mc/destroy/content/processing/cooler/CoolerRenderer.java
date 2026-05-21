package petrolpark.mc.destroy.content.processing.cooler;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.client.DestroyPartials;
import petrolpark.mc.destroy.content.processing.cooler.CoolerBlockEntity.ColdnessLevel;

/**
 * Cooler BER — animated stray skull head that bobs/sways + shivers while FROSTING + rotates to
 * track the nearby player (mirrors Create's BlazeBurner head animation). The head is a
 * {@link DestroyPartials#STRAY_SKULL} PartialModel painted through catnip's
 * {@link SuperByteBuffer} pipeline.
*/
public class CoolerRenderer extends SafeBlockEntityRenderer<CoolerBlockEntity> {

    public CoolerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(CoolerBlockEntity cooler, float partialTicks, PoseStack ms,
                              MultiBufferSource bufferSource, int light, int overlay) {
        ColdnessLevel coldnessLevel = cooler.getColdnessFromBlock();
        if (coldnessLevel == ColdnessLevel.NONE) return;

        Level level = cooler.getLevel();
        BlockState blockState = cooler.getBlockState();
        float animation = cooler.getHeadAnimation().getValue(partialTicks) * 0.175f;
        float horizontalAngle = AngleHelper.rad(cooler.getHeadAngle().getValue(partialTicks));
        int hashCode = cooler.hashCode();

        renderShared(ms, null, bufferSource, level, blockState, coldnessLevel, animation, horizontalAngle, hashCode);
    }

    /** Render both in the World and in Contraptions.*/
    private static void renderShared(PoseStack ms, @Nullable PoseStack modelTransform,
                                     MultiBufferSource bufferSource, Level level, BlockState blockState,
                                     ColdnessLevel coldnessLevel, float headAnimation, float headAngle,
                                     int hashCode) {
        float time = AnimationTickHolder.getRenderTime(level);
        float renderTick = time + (hashCode % 13) * 16f;
        float bobbing = Mth.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / 64f;
        float shivering = (coldnessLevel == ColdnessLevel.FROSTING
            ? Mth.sin((float) ((renderTick * 3) % (2 * Math.PI))) / 24f : 0f)
            * Mth.sin((renderTick / 8) % (2 * Mth.PI));
        float headY = bobbing - (headAnimation * 0.75f);
        headAngle += shivering;

        ms.pushPose();
        SuperByteBuffer headBuffer = CachedBuffers.partial(DestroyPartials.STRAY_SKULL, blockState);
        if (modelTransform != null) headBuffer.transform(modelTransform);
        headBuffer.translate(0, headY, 0);
        draw(headBuffer, headAngle, ms, bufferSource.getBuffer(RenderType.cutout()));
        ms.popPose();
    }

    private static void draw(SuperByteBuffer buffer, float horizontalAngle, PoseStack ms, VertexConsumer vc) {
        buffer.rotateCentered(horizontalAngle, Direction.UP)
            .light(LightTexture.FULL_BRIGHT)
            .renderInto(ms, vc);
    }
}
