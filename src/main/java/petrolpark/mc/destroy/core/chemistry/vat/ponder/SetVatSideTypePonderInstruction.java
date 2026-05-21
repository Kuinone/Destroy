package petrolpark.mc.destroy.core.chemistry.vat.ponder;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;

import petrolpark.mc.destroy.core.chemistry.vat.VatSideBlockEntity;
import petrolpark.mc.destroy.core.chemistry.vat.VatSideBlockEntity.DisplayType;

/**
 * One-shot {@link PonderInstruction} that switches the {@link DisplayType} of a
 * {@link VatSideBlockEntity} at the given {@link BlockPos} in a Ponder scene world. Used by Vat
 * Ponder scenes (future S?? vatFluids / vatItems) to demonstrate switching a side between
 * NORMAL / PIPE / BAROMETER / THERMOMETER / CLOSED_VENT / OPEN_VENT etc.
*/
public class SetVatSideTypePonderInstruction extends PonderInstruction {

    public final BlockPos blockPos;
    public final VatSideBlockEntity.DisplayType displayType;

    public SetVatSideTypePonderInstruction(BlockPos blockPos, DisplayType displayType) {
        this.blockPos = blockPos;
        this.displayType = displayType;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        if (scene.getWorld().getBlockEntity(blockPos) instanceof VatSideBlockEntity vatSide)
            vatSide.setDisplayType(displayType);
    }
}
