package petrolpark.mc.destroy.core.chemistry.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * BER for {@link ElementTankBlockEntity} — renders the held fluid as a box filling the tank
 * proportionally to the fluid level (1/16 → 15/16 on X/Z · dynamic Y based on fluidLevel).
*/
public class ElementTankRenderer extends SafeBlockEntityRenderer<ElementTankBlockEntity> {

    public ElementTankRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(ElementTankBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource bufferSource, int light, int overlay) {
        if (be.getRenderedFluid().isEmpty()) return;
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
            be.getRenderedFluid(),
            1 / 16f, 1 / 16f, 1 / 16f,
            15 / 16f, (1f + 14f * be.getFluidLevel(partialTicks)) / 16f, 15 / 16f,
            bufferSource, ms, light, true, true);
    }
}
