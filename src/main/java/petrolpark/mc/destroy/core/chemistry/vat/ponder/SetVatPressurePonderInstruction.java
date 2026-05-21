package petrolpark.mc.destroy.core.chemistry.vat.ponder;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;

/**
 * One-shot {@link PonderInstruction} that drives the {@code pressure} {@link LerpedFloat} of a
 * {@link petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity VatControllerBlockEntity}
 * toward a target value using {@link LerpedFloat.Chaser#EXP EXP} easing. Used by Vat Ponder scenes
 * (future S?? vatPressure) to animate pressure gauges.
*/
public class SetVatPressurePonderInstruction extends PonderInstruction {

    public final BlockPos vatControllerPos;
    public final float targetPressure;
    public final float chaseSpeed;

    public SetVatPressurePonderInstruction(BlockPos vatControllerPos, float targetPressure, float chaseSpeed) {
        this.vatControllerPos = vatControllerPos;
        this.targetPressure = targetPressure;
        this.chaseSpeed = chaseSpeed;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        scene.getWorld().getBlockEntity(vatControllerPos, DestroyBlockEntityTypes.VAT_CONTROLLER.get()).ifPresent(vc -> {
            vc.pressure.chase(targetPressure, chaseSpeed, LerpedFloat.Chaser.EXP);
        });
    }
}
