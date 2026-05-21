package petrolpark.mc.destroy.chemistry.legacy.genericreaction;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.chemistry.api.error.ChemistryException;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction.ReactionBuilder;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies.MoleculeBuilder;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;

/**
 * A Reaction generator that accepts any Molecule containing a certain functional Group and produces a
 * new Molecule using the rules defined in the generator.
*/
public abstract class GenericReaction {

    /** The set of all Generic Reactions known to Destroy.*/
    public static Set<GenericReaction> GENERIC_REACTIONS = new HashSet<>();

    /** The identifier for this Generic Reaction, which JEI uses to find the title and description.*/
    public final ResourceLocation id;

    /** The example Reaction to be displayed in JEI.*/
    private LegacyReaction exampleReaction;

    public GenericReaction(ResourceLocation id) {
        this.id = id;
    }

    public abstract boolean involvesSingleGroup();

    /**
 * Whether all necessary catalysts and non-generic Reactants are present.
 * @return {@code true} to go on and calculate actual Reactions for generic Reactants in this Mixture
*/
    public abstract boolean isPossibleIn(ReadOnlyMixture mixture);

    public LegacyReaction getExampleReaction() {
        if (exampleReaction == null) exampleReaction = generateExampleReaction();
        if (exampleReaction == null) throw new GenericReactionGenerationException(
            "Could not generate example reaction for Generic Reaction '" + id.toString()
                + "'- reaction generator returned null, which is only allowed in Mixtures.");
        return exampleReaction;
    }

    /**
 * Generate a Reaction to be used to exemplify this Reaction.
*/
    @NotNull
    protected abstract LegacyReaction generateExampleReaction();

    protected static MoleculeBuilder moleculeBuilder() {
        return new MoleculeBuilder("novel");
    }

    protected static ReactionBuilder reactionBuilder() {
        return LegacyReaction.generatedReactionBuilder();
    }

    /**
 * Instantiate a Chemistry Exception. These are silently swallowed by the Mixture generator.
*/
    protected GenericReactionGenerationException exception(String string) {
        return new GenericReactionGenerationException("Problem generating "
            + (involvesSingleGroup() ? "single" : "double") + "-Group Generic Reaction '"
            + id.toString() + "': " + string);
    }

    public class GenericReactionGenerationException extends ChemistryException {

        public GenericReactionGenerationException(String message) {
            super(message);
        }
    }
}
