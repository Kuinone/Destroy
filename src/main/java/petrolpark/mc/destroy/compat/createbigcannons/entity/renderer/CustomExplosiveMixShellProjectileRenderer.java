package petrolpark.mc.destroy.compat.createbigcannons.entity.renderer;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileRenderer;

import petrolpark.mc.destroy.client.DestroyPartials;
import petrolpark.mc.destroy.compat.createbigcannons.entity.CustomExplosiveMixShellProjectile;

/**
 * 3D renderer for the shell projectile. Renders two overlaid partial models from Destroy
 * ({@link DestroyPartials#CUSTOM_EXPLOSIVE_MIX_SHELL_BASE} + {@code _OVERLAY}); base is tinted by
 * the shell's dye color, overlay is unshaded.
*/
public class CustomExplosiveMixShellProjectileRenderer extends BigCannonProjectileRenderer<CustomExplosiveMixShellProjectile> {

    public CustomExplosiveMixShellProjectileRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(CustomExplosiveMixShellProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        BlockState blockState = entity.getRenderedBlockState();
        VertexConsumer vc = buffers.getBuffer(RenderType.cutout());

        poseStack.pushPose();

        Quaternionf q = Axis.YP.rotationDegrees(entity.getViewYRot(partialTicks) + 180.0f);
        Quaternionf q1 = Axis.XP.rotationDegrees(entity.getViewXRot(partialTicks) - 90.0f);
        q.mul(q1);

        poseStack.translate(0.0d, 0.4d, 0.0d);
        poseStack.mulPose(q);

        CachedBuffers.partialFacing(DestroyPartials.CUSTOM_EXPLOSIVE_MIX_SHELL_BASE, blockState)
            .light(packedLight)
            .color(entity.color)
            .renderInto(poseStack, vc);
        CachedBuffers.partialFacing(DestroyPartials.CUSTOM_EXPLOSIVE_MIX_SHELL_OVERLAY, blockState)
            .light(packedLight)
            .renderInto(poseStack, vc);

        poseStack.popPose();
    }
}
