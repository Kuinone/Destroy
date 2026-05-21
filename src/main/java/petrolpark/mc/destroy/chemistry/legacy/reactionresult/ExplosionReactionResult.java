package petrolpark.mc.destroy.chemistry.legacy.reactionresult;

import java.util.function.BiFunction;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReactionResult;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity;
import petrolpark.mc.destroy.core.explosion.SmartExplosion;

/**
 * A {@link ReactionResult} that triggers a {@link SmartExplosion} when the Reaction fires. Commonly
 * paired with destructive chemistry (e.g. TNT synthesis, rapid exotherm from hydrogen peroxide).
*/
public class ExplosionReactionResult extends ReactionResult {

    protected final BiFunction<Level, Vec3, SmartExplosion> explosionFactory;

    /**
 * Convenience factory for a small explosion (radius 2, irregularity 0.5). Use as a
 * {@code BiFunction<Float, LegacyReaction, ReactionResult>} method reference:
 * {@code ExplosionReactionResult::small}.
*/
    public static ExplosionReactionResult small(Float moles, LegacyReaction reaction) {
        return new ExplosionReactionResult(moles, reaction,
            (level, pos) -> new SmartExplosion(level, null, null, null, pos, 2, 0.5f));
    }

    public ExplosionReactionResult(float moles, LegacyReaction reaction,
                                    BiFunction<Level, Vec3, SmartExplosion> explosionFactory) {
        super(moles, reaction);
        this.explosionFactory = explosionFactory;
    }

    @Override
    public void onBasinReaction(Level level, BasinBlockEntity basin) {
        if (level instanceof ServerLevel serverLevel) {
            SmartExplosion.explode(serverLevel,
                explosionFactory.apply(serverLevel, VecHelper.getCenterOf(basin.getBlockPos())));
        }
    }

    @Override
    public void onVatReaction(Level level, VatControllerBlockEntity vatController) {
        vatController.explode(explosionFactory);
    }
}
