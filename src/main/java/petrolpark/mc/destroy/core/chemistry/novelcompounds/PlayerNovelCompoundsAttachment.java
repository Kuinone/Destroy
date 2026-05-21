package petrolpark.mc.destroy.core.chemistry.novelcompounds;

import java.util.HashSet;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.entity.player.Player;

import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyStats;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

/**
 * Per-Player record of every novel (auto-generated from Generic Reactions) Molecule ever synthesized.
 * Stored as a set of FROWNS code strings — each unique novel Molecule that shows up in the player's
 * game counts once toward the {@code NOVEL_COMPOUNDS_SYNTHESIZED} stat.
 *
 * <p>See {@link DestroyAttachmentTypes#PLAYER_NOVEL_COMPOUNDS} for registration (with copyOnDeath).</p>
*/
public class PlayerNovelCompoundsAttachment {

    /**
 * Persistent codec. Stores the set as a list internally (Codec-friendly), with a setLast-wins
 * rebuild on load. Empty list → empty attachment.
*/
    public static final Codec<PlayerNovelCompoundsAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().optionalFieldOf("frowns_strings", java.util.List.of())
            .forGetter(a -> java.util.List.copyOf(a.novelCompoundFROWNSStrings))
    ).apply(instance, PlayerNovelCompoundsAttachment::fromSaved));

    private final Set<String> novelCompoundFROWNSStrings;

    public PlayerNovelCompoundsAttachment() {
        this.novelCompoundFROWNSStrings = new HashSet<>();
    }

    private static PlayerNovelCompoundsAttachment fromSaved(java.util.List<String> saved) {
        PlayerNovelCompoundsAttachment attachment = new PlayerNovelCompoundsAttachment();
        attachment.novelCompoundFROWNSStrings.addAll(saved);
        return attachment;
    }

    /**
 * Record a novel Molecule as synthesized by the given player. If this is the first time this
 * FROWNS code has ever been seen by this player, also increment the
 * {@link DestroyStats#NOVEL_COMPOUNDS_SYNTHESIZED} stat counter.
*/
    public static void add(Player player, LegacySpecies novelCompound) {
        PlayerNovelCompoundsAttachment attachment = player.getData(DestroyAttachmentTypes.PLAYER_NOVEL_COMPOUNDS);
        if (attachment.novelCompoundFROWNSStrings.add(novelCompound.getFROWNSCode())) {
            player.awardStat(DestroyStats.NOVEL_COMPOUNDS_SYNTHESIZED.get());
        }
    }

    public Set<String> getFrownsStrings() {
        return java.util.Collections.unmodifiableSet(novelCompoundFROWNSStrings);
    }
}
