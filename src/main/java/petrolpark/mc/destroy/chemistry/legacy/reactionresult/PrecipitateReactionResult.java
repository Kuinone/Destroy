package petrolpark.mc.destroy.chemistry.legacy.reactionresult;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReactionResult;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity;

/**
 * A {@link ReactionResult} that deposits a precipitate ({@link ItemStack}) when the Reaction fires.
 * Basin path: delegated to {@code ReactionInBasinRecipe} — this class is intentionally a no-op there
 * because the Basin recipe layer reads the precipitate via {@link #getPrecipitate()} (called from
 * {@link ReactionResult#getAllPrecipitates()}). Vat path: inserts directly into
 * {@link VatControllerBlockEntity#inventory}.
*/
public class PrecipitateReactionResult extends ReactionResult {

    private final Supplier<ItemStack> precipitate;

    /**
 * Factory for the chemistry Reaction pathway. Use as a method reference:
 * {@code PrecipitateReactionResult.of(DestroyItems.FOO::asStack)} yields a
 * {@code BiFunction<Float, LegacyReaction, ReactionResult>} suitable for
 * {@link LegacyReaction.ReactionBuilder#withResult(float, BiFunction)}.
*/
    public static BiFunction<Float, LegacyReaction, ReactionResult> of(Supplier<ItemStack> precipitate) {
        return (m, r) -> new PrecipitateReactionResult(m, r, precipitate);
    }

    public PrecipitateReactionResult(float moles, LegacyReaction reaction, Supplier<ItemStack> precipitate) {
        super(moles, reaction);
        this.precipitate = precipitate;
    }

    public ItemStack getPrecipitate() {
        return precipitate.get();
    }

    @Override
    public void onBasinReaction(Level level, BasinBlockEntity basin) {
        // Intentional no-op. Basin path reads precipitate via getAllPrecipitates() in
        // ReactionInBasinRecipe.
    }

    @Override
    public void onVatReaction(Level level, VatControllerBlockEntity vatController) {
        ItemHandlerHelper.insertItemStacked(vatController.inventory, precipitate.get(), false);
    }

    @Override
    public Collection<PrecipitateReactionResult> getAllPrecipitates() {
        return Collections.singleton(this);
    }
}
