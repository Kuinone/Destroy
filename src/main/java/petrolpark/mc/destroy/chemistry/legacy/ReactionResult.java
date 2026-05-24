package petrolpark.mc.destroy.chemistry.legacy;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import net.minecraft.world.level.Level;

import petrolpark.mc.destroy.chemistry.legacy.reactionresult.PrecipitateReactionResult;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity;

/**
 * Abstract base for side-effects of a {@link LegacyReaction} firing — e.g. precipitating an
 * Item Stack, triggering an advancement, igniting an explosion. Subclasses implement
 * {@link #onBasinReaction(Level, BasinBlockEntity)} + {@link #onVatReaction(Level, VatControllerBlockEntity)}
 * for the two host-block types Destroy recognizes.
*/
public abstract class ReactionResult {

    protected final LegacyReaction reaction;
    protected final float moles;

    private final boolean oneOff;

    /**
 * @param moles How many moles of {@link LegacyReaction} must take place before this Reaction Result
 * occurs. If this is {@code 0f}, then any amount of Reaction occurring will trigger the result once.
 * @param reaction The Reaction which results in this
*/
    public ReactionResult(float moles, LegacyReaction reaction) {
        this.moles = moles;
        this.reaction = reaction;
        oneOff = moles == 0f;
    }

    /**
 * Get the number of moles of Reaction which have to take place before this Reaction Result occurs.
*/
    public float getRequiredMoles() {
        return moles;
    }

    public final Optional<LegacyReaction> getReaction() {
        return Optional.ofNullable(reaction);
    }

    /**
 * Whether this Reaction Result occurs when <em>any</em> amount of Reaction occurs.
*/
    public boolean isOneOff() {
        return oneOff;
    }

    /**
 * Do something when the Reaction finishes in a Basin.
*/
    public abstract void onBasinReaction(Level level, BasinBlockEntity basin);

    /**
 * Do something when the Reaction finishes in a Vat.
*/
    public abstract void onVatReaction(Level level, VatControllerBlockEntity vatController);

    public Collection<PrecipitateReactionResult> getAllPrecipitates() {
        return Collections.emptySet();
    }

    /**
     * Factory signature for {@link LegacyReaction.ReactionBuilder#withResult(float, java.util.function.BiFunction)}.
     * Named alias of {@code BiFunction<Float, LegacyReaction, ReactionResult>} so that data-driven
     * codec implementations can carry it as a typed field.
     */
    @FunctionalInterface
    public interface Factory extends java.util.function.BiFunction<Float, LegacyReaction, ReactionResult> {}
}
