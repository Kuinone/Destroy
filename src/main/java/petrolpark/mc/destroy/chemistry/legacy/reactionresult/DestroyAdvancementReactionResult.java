package petrolpark.mc.destroy.chemistry.legacy.reactionresult;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import net.minecraft.world.level.Level;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReactionResult;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity;

/**
 * A {@link ReactionResult} that awards a {@link DestroyAdvancementTrigger} when the Reaction has
 * produced enough of its target molar amount.
 *
 * <p>This stub just holds the trigger reference; {@code onBasinReaction} / {@code onVatReaction} are
 * no-ops. The trigger itself can still be referenced via method-reference
 * ({@code DestroyAdvancementTrigger.ACETONE::asReactionResult}) without NPE.</p>
*/
public class DestroyAdvancementReactionResult extends ReactionResult {

    @SuppressWarnings("unused")
    private final DestroyAdvancementTrigger.Stub advancement;

    public DestroyAdvancementReactionResult(float moles, LegacyReaction reaction,
                                             DestroyAdvancementTrigger.Stub advancement) {
        super(moles, reaction);
        this.advancement = advancement;
    }

    @Override
    public void onBasinReaction(Level level, BasinBlockEntity basin) {
    }

    @Override
    public void onVatReaction(Level level, VatControllerBlockEntity vatController) {
    }
}
