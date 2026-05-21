package petrolpark.mc.destroy.chemistry.legacy;

import javax.annotation.Nullable;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import petrolpark.mc.destroy.client.DestroyPartials;

/**
 * A specific Atom in a specific {@code LegacySpecies} (aka Molecule). Atoms can be rearranged
 * and added to {@code LegacyMolecularStructure} (formulae), but <b>never modify Atoms themselves</b>.
 * Also note that {@link LegacyElement#HYDROGEN} atoms are not conserved — the Hydrogens a Molecule
 * was created with will not necessarily be carried over when accessed from different points.
*/
public class LegacyAtom {

    /**
 * The {@link LegacyElement specific isotope} of this Atom.
*/
    private final LegacyElement element;

    /**
 * If this 'Atom' is an {@link LegacyElement#R_GROUP R-group} used to display a generic reaction,
 * this number indicates which one it is.
*/
    public int rGroupNumber;

    /**
 * The charge of this Atom, relative to a proton.
*/
    public final double formalCharge;

    public LegacyAtom(LegacyElement element) {
        this(element, 0);
    }

    public LegacyAtom(LegacyElement element, double formalCharge) {
        this.element = element;
        this.formalCharge = formalCharge;
    }

    public LegacyElement getElement() {
        return element;
    }

    /**
 * Client-side only: the ball model this atom renders as. For R-groups, maps
 * {@link #rGroupNumber} to the numbered R-model list (1..9) or the generic R-group.
 * For regular elements, delegates to {@link LegacyElement#getPartial()}. Returns
 * {@code null} on servers or before {@link DestroyPartials} has finished static init.
*/
    @Nullable
    public PartialModel getPartial() {
        if (element == LegacyElement.R_GROUP) {
            if (rGroupNumber < 10 && rGroupNumber >= 1) {
                return DestroyPartials.rGroups.get(rGroupNumber);
            }
            return DestroyPartials.R_GROUP;
        } else {
            return element.getPartial();
        }
    }

    /**
 * Whether this Atom is a {@link LegacyElement#HYDROGEN} with no {@link #formalCharge formal charge}.
*/
    public Boolean isNeutralHydrogen() {
        return element == LegacyElement.HYDROGEN && formalCharge == 0;
    }
}
