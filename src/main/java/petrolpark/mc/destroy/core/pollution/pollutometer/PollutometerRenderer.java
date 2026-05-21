package petrolpark.mc.destroy.core.pollution.pollutometer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.client.DestroyPartials;

/**
 * Renders the Pollutometer's spinning anemometer + weathervane on top of the block. Both partials
 * spin around the Y-axis with offsets derived from {@link AnimationTickHolder#getRenderTime};
*/
public class PollutometerRenderer extends SafeBlockEntityRenderer<PollutometerBlockEntity> {

    public PollutometerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(PollutometerBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        float renderTime = AnimationTickHolder.getRenderTime(be.getLevel());
        // Per-instance hash offset so adjacent pollutometers spin at de-synchronised phases.
        float t = renderTime + (be.hashCode() % 13) * 16f;

        VertexConsumer vc = buffer.getBuffer(RenderType.cutoutMipped());

        ms.pushPose();
        SuperByteBuffer anemometer = CachedBuffers.partial(DestroyPartials.POLLUTOMETER_ANEMOMETER, state);
        draw(anemometer, -t / 8f, ms, vc, light);
        ms.popPose();

        ms.pushPose();
        SuperByteBuffer weathervane = CachedBuffers.partial(DestroyPartials.POLLUTOMETER_WEATHERVANE, state);
        // Constant 45° + sinusoidal wind jitter (period ≈ 16t, amplitude 1/24 of full rotation).
        float weathervaneAngle = 0.7853982f + Mth.sin((float)((t / 16f) % (2 * Math.PI))) / 24f;
        draw(weathervane, weathervaneAngle, ms, vc, light);
        ms.popPose();
    }

    private static void draw(SuperByteBuffer sbb, float angle, PoseStack ms, VertexConsumer vc, int light) {
        sbb.rotateCentered(angle, Direction.UP)
            .light(light)
            .renderInto(ms, vc);
    }
}
