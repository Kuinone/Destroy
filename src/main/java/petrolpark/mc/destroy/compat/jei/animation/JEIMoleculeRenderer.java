package petrolpark.mc.destroy.compat.jei.animation;

import mezz.jei.api.gui.drawable.IDrawable;

import net.minecraft.client.gui.GuiGraphics;

import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.core.chemistry.MoleculeRenderer;

/**
 * JEI {@link IDrawable} adapter for Destroy's {@link MoleculeRenderer} · renders a 2D molecular
 * structure inside a JEI recipe category slot. Delegates to {@link MoleculeRenderer#render} in
 * the {@code draw} callback.
*/
public class JEIMoleculeRenderer extends MoleculeRenderer implements IDrawable {

    public JEIMoleculeRenderer(LegacySpecies molecule) {
        super(molecule);
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        render(xOffset, yOffset, graphics);
    }
}
