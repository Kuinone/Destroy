package petrolpark.mc.destroy.content.processing.trypolithography.recipe;

/**
 * Marker interface for recipes that confer a "circuit pattern" to their output ItemStack.
 * Empty interface; Future consumers use {@code instanceof} checks to
 * identify pattern-transferring recipes in the Keypunch / CircuitPuncher pipelines.
*/
public interface IConfersCircuitPatternRecipe {
}
