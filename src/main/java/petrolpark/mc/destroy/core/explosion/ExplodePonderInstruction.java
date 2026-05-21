package petrolpark.mc.destroy.core.explosion;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

/**
 * One-shot {@link PonderInstruction} that creates an {@link Explosion} in a Ponder scene world
 * via a caller-supplied factory + runs the usual {@code explode()} + {@code finalizeExplosion(true)}
 * sequence. Used by Vat Ponder scenes (e.g. {@code vatPressure}) to demonstrate over-pressure
 * explosion, and potentially by Explosives Ponder scenes.
*/
public class ExplodePonderInstruction extends PonderInstruction {

    public final ExplosionFactory explosionFactory;

    public ExplodePonderInstruction(ExplosionFactory explosion) {
        this.explosionFactory = explosion;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        Explosion explosion = explosionFactory.create(scene.getWorld());
        explosion.explode();
        explosion.finalizeExplosion(true);
    }

    @FunctionalInterface
    public static interface ExplosionFactory {

        Explosion create(Level level);
    }
}
