package petrolpark.mc.destroy.chemistry.legacy;

import javax.annotation.Nullable;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/**
 * A directional covalent Bond between two {@link LegacyAtom Atoms}. Within a
 * {@code LegacyMolecularStructure}, each 'bonded' Atom has its own Bond object associated with it.
 * These are not the same object — each Bond is in an {@link LegacyBond#getMirror opposite direction}.
*/
public class LegacyBond {

    private volatile BondType type;
    private final LegacyAtom srcAtom;
    private final LegacyAtom destAtom;

    public LegacyBond(LegacyAtom sourceAtom, LegacyAtom destinationAtom, BondType type) {
        this.srcAtom = sourceAtom;
        this.destAtom = destinationAtom;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LegacyBond otherBond)) return false;
        return (type == otherBond.type && srcAtom == otherBond.srcAtom && destAtom == otherBond.destAtom);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + System.identityHashCode(srcAtom);
        result = 31 * result + System.identityHashCode(destAtom);
        return result;
    }

    public LegacyAtom getSourceAtom() {
        return srcAtom;
    }

    public LegacyAtom getDestinationAtom() {
        return destAtom;
    }

    public BondType getType() {
        return type;
    }

    public void setType(BondType type) {
        this.type = type;
    }

    /**
 * As Bonds are directed, this returns a Bond in the other direction. The Bond will be of the
 * same {@link BondType type}.
 *
 * <p>This method instantiates a new Bond — if the Bond is already part of a structure, this
 * will not return the pre-existing mirror Bond. Strictly this pre-existing Bond should never
 * be required, except when removing an Atom from a structure.</p>
*/
    public LegacyBond getMirror() {
        return new LegacyBond(destAtom, srcAtom, type);
    }

    /**
 * A 'type' of {@link LegacyBond}: single, double, triple, or aromatic. Different types render
 * differently and the {@link BondType#getEquivalent single-bond-equivalent} of
 * the Bonds to an Atom is used to determine valency when auto-adding Hydrogens to a structure.
*/
    public enum BondType {
        /** 2-center-2-electron covalent Bond.*/
        SINGLE(1f, ""),
        /** 2-center-4-electron covalent Bond.*/
        DOUBLE(2f, "="),
        /** 2-center-6-electron covalent Bond.*/
        TRIPLE(3f, "#"),
        /** Best represented in Lewis structure as a 2-center-3-electron covalent Bond.*/
        AROMATIC(1.5f, "~");

        private final float singleBondEquivalent;
        private final String FROWNSCode;

        /**
 * Client-side only: the stick model this bond renders as. Assigned in
 * {@link petrolpark.mc.destroy.client.DestroyPartials}'s static init. Stays {@code null}
 * on servers.
*/
        @Nullable
        private PartialModel partial;

        BondType(float singleBondEquivalent, String FROWNSCode) {
            this.singleBondEquivalent = singleBondEquivalent;
            this.FROWNSCode = FROWNSCode;
        }

        /** Client-side stick model for this bond type. May be {@code null} on servers.*/
        @Nullable
        public PartialModel getPartial() {
            return partial;
        }

        /** Sets the client-side stick model — called from {@code DestroyPartials}'s static block.*/
        public void setPartial(PartialModel partial) {
            this.partial = partial;
        }

        /**
 * Number of {@link BondType#SINGLE single} {@link LegacyBond Bonds} this type is
 * equivalent to (essentially the bond order).
*/
        public float getEquivalent() {
            return singleBondEquivalent;
        }

        /**
 * Character used to represent this Bond in
 * <a href="https://github.com/petrolpark/Destroy/wiki/FROWNS">FROWNS</a> if applicable.
 * For a {@link BondType#SINGLE single Bond}, returns an empty String.
*/
        public String getFROWNSCode() {
            return FROWNSCode;
        }

        /**
 * Get a type of {@link LegacyBond} from a character in a FROWNS code.
 *
 * @param c symbol representing a bond type
 * @return Unrecognised chars default to {@link BondType#SINGLE}.
*/
        public static BondType fromFROWNSCode(char c) {
            return switch (c) {
                case '=' -> DOUBLE;
                case '#' -> TRIPLE;
                case '~' -> AROMATIC;
                default -> SINGLE;
            };
        }
    }
}
