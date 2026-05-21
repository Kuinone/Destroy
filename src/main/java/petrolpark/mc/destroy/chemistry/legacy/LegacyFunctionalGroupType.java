package petrolpark.mc.destroy.chemistry.legacy;

import java.util.function.Supplier;

import com.google.common.base.MoreObjects;

import petrolpark.mc.destroy.chemistry.api.error.ChemistryException.ExampleMoleculeMissingGroupException;

/**
 * A {@link LegacyFunctionalGroup} type. Each type is instantiated once during mod setup, typically as a
 * {@code public static final} constant in {@link petrolpark.mc.destroy.chemistry.legacy.index.DestroyGroupTypes
 * DestroyGroupTypes}. Types carry a lazy supplier for an "example Molecule" that must contain at least one
 * occurrence of the group — this is checked lazily on first access to catch topology/group definition mistakes.
*/
public class LegacyFunctionalGroupType<G extends LegacyFunctionalGroup<G>> {

    private final Supplier<LegacySpecies> exampleMolecule;
    private boolean exampleMoleculeVerified = false;

    public LegacyFunctionalGroupType(Supplier<LegacySpecies> exampleMoleculeSupplier) {
        this.exampleMolecule = exampleMoleculeSupplier;
    }

    public LegacySpecies getExampleMolecule() {
        if (!exampleMoleculeVerified) verifyExampleMolecule();
        return exampleMolecule.get();
    }

    private void verifyExampleMolecule() {
        if (!exampleMolecule.get().getFunctionalGroups().stream()
            .anyMatch(group -> group.getType() == this)) {
            throw new ExampleMoleculeMissingGroupException(exampleMolecule.get());
        }
        exampleMoleculeVerified = true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("Example Molecule", getExampleMolecule().getFullID())
            .toString();
    }
}
