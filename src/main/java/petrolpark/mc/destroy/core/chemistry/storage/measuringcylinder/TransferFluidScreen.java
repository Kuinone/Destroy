package petrolpark.mc.destroy.core.chemistry.storage.measuringcylinder;

import java.util.Collections;

import com.mojang.blaze3d.platform.Window;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.client.DestroyLang;

/**
 * Slider-enabled GUI for MeasuringCylinder fluid transfer. Extends Create's {@link ValueSettingsScreen}
 * to reuse its brass-frame slider UI (0 mB → maxTransfer mB · 50 mB milestones · millibucket unit).
 *
 * <p>Save action: override {@link #saveAndClose} to send a {@link TransferFluidC2SPacket} with the
 * selected mB amount + direction (block→item or item→block) + hand.</p>
*/
public class TransferFluidScreen extends ValueSettingsScreen {

    protected final BlockPos pos;
    protected final Direction face;
    protected final InteractionHand hand;
    protected final boolean blockToItem;

    public TransferFluidScreen(BlockPos pos, Direction face, InteractionHand hand,
                               int maxTransfer, boolean blockToItem) {
        super(pos, buildBoard(maxTransfer, blockToItem),
              new ValueSettings(0, maxTransfer),
              s -> {},  // onHover no-op
              0);  // netId — not a standard Create BE's ValueSettings path
        this.pos = pos;
        this.face = face;
        this.hand = hand;
        this.blockToItem = blockToItem;
    }

    private static ValueSettingsBoard buildBoard(int maxTransfer, boolean blockToItem) {
        Component blockName = Component.literal(blockToItem ? "Block" : "Item");
        Component itemName = Component.literal(blockToItem ? "Item" : "Block");
        return new ValueSettingsBoard(
            DestroyLang.translate("tooltip.measuring_cylinder", blockName, itemName).component(),
            maxTransfer,
            50,  // milestone every 50 mB
            Collections.singletonList(CreateLang.translateDirect("generic.unit.millibuckets")),
            new ValueSettingsFormatter(ValueSettings::format));
    }

    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (blockToItem && minecraft.options.keyAttack.matches(keyCode, scanCode)) {
            Window window = minecraft.getWindow();
            double x = minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
            double y = minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
            saveAndClose(x, y);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (blockToItem && minecraft.options.keyAttack.matchesMouse(button)) {
            saveAndClose(mouseX, mouseY);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void saveAndClose(double mouseX, double mouseY) {
        ValueSettings closest = getClosestCoordinate((int) mouseX, (int) mouseY);
        DestroyPackets.sendToServer(new TransferFluidC2SPacket(pos, face,
            hand == InteractionHand.MAIN_HAND, blockToItem, closest.value()));
        onClose();
    }
}
