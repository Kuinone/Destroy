package petrolpark.mc.destroy.content.processing.trypolithography;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.AnimatedOverlayElementBase;
import net.createmod.ponder.foundation.instruction.FadeInOutInstruction;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import petrolpark.mc.destroy.client.DestroyGuiTextures;

/**
 * Custom Ponder instruction that draws a 4×4 circuit pattern overlay inside an animated speech
 * box at a given scene-space coordinate. Used by {@link TrypolithographyPonderScenes} to show
 * the circuit pattern state (e.g. after each keypunch) as an inline annotation on belt items.
 *
 * <p>Design pattern: an instance of this instruction adds a {@link CircuitMaskElement} to the
 * scene on {@link #show}, and flips its visibility on {@link #hide}. The element's
 * {@link CircuitMaskElement#render render} is the actual drawing — a speech-box animation
 * (via {@link PonderUI#renderSpeechBox}) wrapping the 4×4 pattern render (via
 * {@link CircuitPatternTooltipComponent#renderCircuitMask}). Fade-in/out with a 10-px
 * directional offset from the pointing axis (like Create's InputWindowElement).</p>
*/
public class ShowCircuitPatternPonderInstruction extends FadeInOutInstruction {

    private final Vec3 sceneSpace;
    private final Pointing direction;
    private final int pattern;

    private final CircuitMaskElement element;

    public ShowCircuitPatternPonderInstruction(Pointing direction, Vec3 sceneSpace, int pattern, int ticks) {
        super(ticks);
        this.sceneSpace = sceneSpace;
        this.direction = direction;
        this.pattern = pattern;
        this.element = new CircuitMaskElement();
    }

    @Override
    protected void show(PonderScene scene) {
        scene.addElement(element);
        element.setVisible(true);
    }

    @Override
    protected void hide(PonderScene scene) {
        element.setVisible(false);
    }

    @Override
    protected void applyFade(PonderScene scene, float fade) {
        element.setFade(fade);
    }

    public class CircuitMaskElement extends AnimatedOverlayElementBase {

        /**
 * Render path largely adapted from Create's
 * {@code com.simibubi.create.foundation.ponder.element.InputWindowElement}.
*/
        @Override
        public void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade) {
            float xFade = direction == Pointing.RIGHT ? -1 : direction == Pointing.LEFT ? 1 : 0;
            float yFade = direction == Pointing.DOWN ? -1 : direction == Pointing.UP ? 1 : 0;
            xFade *= 10 * (1 - fade);
            yFade *= 10 * (1 - fade);

            if (fade < 1 / 16f) return;
            Vec2 sceneToScreen = scene.getTransform().sceneToScreen(sceneSpace, partialTicks);

            PoseStack ms = graphics.pose();
            ms.pushPose();
            ms.translate(sceneToScreen.x + xFade, sceneToScreen.y + yFade, 400);
            PonderUI.renderSpeechBox(graphics, 0, 0, 48, 48, false, direction, true);
            ms.translate(0, 0, 100);
            CircuitPatternTooltipComponent.renderCircuitMask(graphics, pattern, false,
                DestroyGuiTextures.CIRCUIT_MASK_BORDER,
                DestroyGuiTextures.CIRCUIT_MASK_CELL,
                DestroyGuiTextures.CIRCUIT_MASK_CELL_SHADING);
            ms.translate(0, 0, 100);
            ms.popPose();
        }
    }
}
