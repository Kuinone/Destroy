package petrolpark.mc.destroy.content.oil.seismology;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import petrolpark.mc.destroy.content.oil.seismology.SeismographItem.Seismograph;

/**
 * Interactive GUI for a held {@link SeismographItem}. Displays the 8×8 nonogram with row/column
 * number hints + the underlying vanilla map tile behind it, and handles grid-cell clicks to cycle
 * {@link Seismograph.Mark}: {@code NONE → GUESSED_TICK → GUESSED_CROSS → NONE}. Server-authoritative
 * TICK/CROSS marks (set by {@link SeismometerItem} on explosion) are locked from player input.
*/
public class SeismographScreen extends AbstractSimiScreen {

    private final Minecraft mc;

    private final Seismograph seismograph;
    private final MapId mapId;
    private final MapItemSavedData mapData;
    private final InteractionHand hand;

    public static final int SCALE = 3;

    public SeismographScreen(ItemStack stack, InteractionHand hand) {
        mc = Minecraft.getInstance();

        mapId = stack.get(DataComponents.MAP_ID);
        mapData = MapItem.getSavedData(stack, mc.level);
        seismograph = SeismographItem.readSeismograph(stack);

        this.hand = hand;
    }

    @Override
    protected void init() {
        setWindowSize(64 * SCALE, 64 * SCALE);
        super.init();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = (mouseX - guiLeft) / (double) SCALE;
        double y = (mouseY - guiTop) / (double) SCALE;
        if (seismograph != null && x > 13 && y > 13 && x < 60 && y < 60) {
            int gridX = (int) (x - 13) / 6;
            int gridY = (int) (y - 13) / 6;
            Seismograph.Mark mark = seismograph.getMark(gridX, gridY);
            Seismograph.Mark newMark = null;
            switch (mark) {
                case TICK:
                case CROSS:
                    break; // locked marks (Seismometer-authoritative)
                case NONE:
                    newMark = Seismograph.Mark.GUESSED_TICK;
                    break;
                case GUESSED_TICK:
                    newMark = Seismograph.Mark.GUESSED_CROSS;
                    break;
                case GUESSED_CROSS:
                    newMark = Seismograph.Mark.NONE;
                    break;
            }
            if (newMark != null) {
                seismograph.mark(gridX, gridY, newMark);
                // Client-side preview: won't actually trigger advancement, but will fill in the
                // grid if player has correctly solved the nonogram. Server re-runs on packet receive.
                seismograph.triggerSolveSeismographAdvancement(null, null);
                CatnipServices.NETWORK.sendToServer(
                    new MarkSeismographC2SPacket((byte) gridX, (byte) gridY, newMark, hand == InteractionHand.MAIN_HAND));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(guiLeft, guiTop, 0f);
        ms.scale(3f, 3f, 3f);
        SeismographItemRenderer.renderSeismograph(ms, graphics.bufferSource(), 0xFFFFFF, mapId, mapData, seismograph, mc,
            (t, x, y) -> t.render(ms, x, y), false);
        ms.popPose();
    }
}
