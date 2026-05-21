package petrolpark.mc.destroy.content.logistics.siphon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Siphon BER — renders the held fluid as two stacked boxes: a large floor-level cube (2→11 Y)
 * and a central upper column (floor→fluidTop Y, centered 6→10 on X/Z). Visual feedback of how
 * much fluid the siphon has buffered + the {@code leftToDrain} throughput countdown.
*/
public class SiphonRenderer extends SafeBlockEntityRenderer<SiphonBlockEntity> {

    public SiphonRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(SiphonBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource bufferSource, int light, int overlay) {
        if (be.tank.getPrimaryHandler().isEmpty()) return;
        FluidStack fs = be.tank.getPrimaryHandler().getFluid();
        float fluidTop = 2f + 12f * be.tank.getPrimaryTank().getFluidLevel().getValue(partialTicks);
        if (fluidTop < 11f && be.leftToDrain > 0) {
            NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fs,
                6 / 16f, fluidTop / 16f, 6 / 16f,
                10 / 16f, 11 / 16f - 1 / 128f, 10 / 16f,
                bufferSource, ms, light, false, true);
        }
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fs,
            1 / 16f + 1 / 128f, 2 / 16f, 1 / 16f + 1 / 128f,
            15 / 16f - 1 / 128f, fluidTop / 16f, 15 / 16f - 1 / 128f,
            bufferSource, ms, light, false, true);
    }
}
