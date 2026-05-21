package petrolpark.mc.destroy.chemistry.legacy.reactionresult;

import java.util.Optional;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReactionResult;
import petrolpark.mc.destroy.core.chemistry.novelcompounds.PlayerNovelCompoundsAttachment;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;

/**
 * A {@link ReactionResult} that fires when a new novel (auto-generated) Molecule first appears in a
 * Mixture. Records the synthesis in the placing player's
 * {@link PlayerNovelCompoundsAttachment discovery set} so the {@code NOVEL_COMPOUNDS_SYNTHESIZED}
 * stat counter picks it up.
*/
public class NovelCompoundSynthesizedReactionResult extends ReactionResult {

    public final LegacySpecies novelMolecule;

    public NovelCompoundSynthesizedReactionResult(float moles, LegacyReaction reaction, LegacySpecies novelMolecule) {
        super(moles, reaction);
        this.novelMolecule = novelMolecule;
    }

    @Override
    public void onBasinReaction(Level level, BasinBlockEntity basin) {
        Optional.ofNullable(basin.getBehaviour(DestroyAdvancementBehaviour.TYPE)).ifPresent(behaviour -> {
            Player player = behaviour.getPlayer();
            if (player != null) PlayerNovelCompoundsAttachment.add(player, novelMolecule);
        });
    }

    @Override
    public void onVatReaction(Level level, VatControllerBlockEntity vatController) {
        Player player = vatController.getPlayer();
        if (player != null) PlayerNovelCompoundsAttachment.add(player, novelMolecule);
    }
}
