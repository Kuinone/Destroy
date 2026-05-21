package petrolpark.mc.destroy.client;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeFogColor;
import net.neoforged.neoforge.client.event.ViewportEvent.RenderFog;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.core.pollution.PollutionHelper;

/**
 * Brown smog fog overlay driven by chunk SMOG pollution. Two effects:
 * <ul>
 * <li>{@link RenderFog} — shrinks near/far fog plane distance proportionally to SMOG saturation
 * (0..1 of {@link PollutionType.Properties#max}), simulating reduced visibility.</li>
 * <li>{@link ComputeFogColor} — biases the existing fog color toward {@code #4D2F19} (BROWN)
 * proportionally to SMOG saturation, smoothly lerped via {@link LerpedFloat}.</li>
 * </ul>
*/
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = Destroy.MOD_ID)
public class FogHandler {

    private static final Color BROWN = new Color(0xFF4D2F19);

    /** Singleton — the static event hooks dispatch to this instance.*/
    public static final FogHandler INSTANCE = new FogHandler();

    protected Color targetColor = Color.BLACK;
    protected Color lastColor = Color.TRANSPARENT_BLACK;
    protected LerpedFloat colorMix = LerpedFloat.linear();

    public void tick() {
        colorMix.tickChaser();
        if (colorMix.getValue() >= 1d) lastColor = targetColor;
    }

    public void setTargetColor(Color color, float partialTicks) {
        if (color.equals(targetColor)) return;
        if (lastColor.equals(Color.TRANSPARENT_BLACK)) {
            lastColor = color;
        } else {
            lastColor = getColor(partialTicks);
        }
        targetColor = color;
        colorMix.setValue(0d);
        colorMix.chase(1d, 0.2d, LerpedFloat.Chaser.EXP);
    }

    public Color getColor(float partialTicks) {
        return Color.mixColors(lastColor, targetColor, colorMix.getValue(partialTicks));
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        INSTANCE.tick();
    }

    /** {@link Camera#getFluidInCamera()} doesn't account for modded fluids — broaden the check.*/
    private static FogType getFluidInCamera(Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        FluidState state = mc.level.getFluidState(camera.getBlockPosition());
        if (camera.getPosition().y < camera.getBlockPosition().getY()
                + state.getHeight(mc.level, camera.getBlockPosition())) {
            return FogType.WATER;
        }
        return camera.getFluidInCamera();
    }

    @SubscribeEvent
    public static void renderFog(RenderFog event) {
        if (!smogEnabled()) return;
        if (getFluidInCamera(event.getCamera()) != FogType.NONE) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        var smogType = DestroyPollutionTypes.SMOG.get();
        float smog = (float) PollutionHelper.getPollution(
            mc.level.getChunk(mc.player.blockPosition()), smogType);
        int max = PollutionHelper.getChunkPollutionTypeProperties(smogType).max();
        if (max <= 0) return;
        float ratio = smog / (float) max;
        event.scaleNearPlaneDistance(1f - 0.8f * ratio);
        event.scaleFarPlaneDistance(1f - 0.5f * ratio);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void colorFog(ComputeFogColor event) {
        if (!smogEnabled()) return;
        if (getFluidInCamera(event.getCamera()) != FogType.NONE) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        var smogType = DestroyPollutionTypes.SMOG.get();
        float smog = (float) PollutionHelper.getPollution(
            mc.level.getChunk(mc.player.blockPosition()), smogType);
        int max = PollutionHelper.getChunkPollutionTypeProperties(smogType).max();
        if (max <= 0) return;
        float ratio = smog / (float) max;

        Color existing = new Color(event.getRed(), event.getGreen(), event.getBlue(), 1f);
        INSTANCE.setTargetColor(Color.mixColors(existing, BROWN, 0.8f * ratio),
            AnimationTickHolder.getPartialTicks());
        Color color = INSTANCE.getColor(AnimationTickHolder.getPartialTicks());
        event.setRed(color.getRedAsFloat());
        event.setGreen(color.getGreenAsFloat());
        event.setBlue(color.getBlueAsFloat());
    }

    protected static boolean smogEnabled() {
        return PollutionHelper.isPollutionEnabled()
            && DestroyConfigs.client().pollution.smog.get();
    }
}
