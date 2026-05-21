package petrolpark.mc.destroy.chemistry.legacy;

import java.util.function.Function;

import javax.annotation.Nullable;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import petrolpark.mc.destroy.core.chemistry.MoleculeRenderer.Geometry;

/**
 * Element constants for Destroy's legacy chemistry engine. Each {@code LegacyElement} represents
 * a (sometimes simplified) chemical element usable in a molecular structure: atomic symbol,
 * relative atomic mass, Pauling electronegativity, and the set of common valencies.
*/
public enum LegacyElement {

    // In the order they should appear in empirical formulae
    R_GROUP("R", 0.0001f, 2.5f, new double[]{1, 2, 3}),
    CARBON("C", 12.01f, 2.5f, new double[]{4}),
    HYDROGEN("H", 1.01f, 2.1f, new double[]{1}),
    SULFUR("S", 32.07f, 2.5f, new double[]{2, 0, 4, 6}),
    NITROGEN("N", 14.01f, 3.0f, new double[]{3, 4}),
    OXYGEN("O", 16.00f, 3.5f, new double[]{0, 1.5d, 2}, i -> i == 2 ? Geometry.V_SHAPE : null),
    BORON("B", 10.81f, 2.04f, new double[]{3d}),
    FLUORINE("F", 19.00f, 4.0f, new double[]{1}),
    SODIUM("Na", 23.00f, 0.9f, new double[]{1}),
    CHLORINE("Cl", 35.45f, 3.0f, new double[]{1}),
    POTASSIUM("K", 39.10f, 0.8f, new double[]{1}),
    CALCIUM("Ca", 40.08f, 1.0f, new double[]{2}),
    CHROMIUM("Cr", 52.00f, 1.66f, new double[]{2d, 3d, 6d}),
    IRON("Fe", 55.85f, 1.8f, new double[]{0, 2, 3}),
    NICKEL("Ni", 58.69f, 1.8f, new double[]{1}),
    COPPER("Cu", 63.55f, 1.9f, new double[]{1, 2}),
    ZINC("Zn", 65.38f, 1.6f, new double[]{1}),
    ZIRCONIUM("Zr", 91.22f, 1.4f, new double[]{1}),
    IODINE("I", 126.90f, 2.7f, new double[]{1}),
    PLATINUM("Pt", 195.08f, 2.2f, new double[]{1}),
    GOLD("Au", 196.97f, 2.4f, new double[]{0, 4}),
    MERCURY("Hg", 200.59f, 1.9f, new double[]{2}),
    LEAD("Pb", 207.20f, 1.8f, new double[]{2, 4}),
    ARGON("Ar", 39.95f, 0f, new double[]{0});

    public final String symbol;
    public final Float mass;
    public final Float electronegativity;
    public final double[] valencies;

    /**
 * Optional per-element geometry override. If non-null, queried before the default
 * connection-count → {@link Geometry} mapping in {@link #getGeometry(int)}. Return {@code null}
 * to fall through to the default. Used by OXYGEN to force V_SHAPE for 2-connection oxygens
 * (e.g. in water / ether) instead of the default LINEAR.
*/
    @Nullable
    private final Function<Integer, Geometry> geometryOverride;

    /**
 * Client-side only: the ball model this element renders as in the molecule viewer. Assigned
 * in {@link petrolpark.mc.destroy.client.DestroyPartials}'s static init block. Stays {@code null}
 * on dedicated servers (no one reads it there).
*/
    @Nullable
    private PartialModel partial;

    LegacyElement(String symbol, Float mass, Float electronegativity, double[] valencies) {
        this(symbol, mass, electronegativity, valencies, null);
    }

    LegacyElement(String symbol, Float mass, Float electronegativity, double[] valencies,
                  @Nullable Function<Integer, Geometry> geometryOverride) {
        this.symbol = symbol;
        this.mass = mass;
        this.electronegativity = electronegativity;
        this.valencies = valencies;
        this.geometryOverride = geometryOverride;
    }

    /**
 * Client-side ball model for this element. May be {@code null} on servers or before
 * {@link petrolpark.mc.destroy.client.DestroyPartials} has finished class-initialization.
*/
    @Nullable
    public PartialModel getPartial() {
        return partial;
    }

    /**
 * Sets the client-side ball model — called from
 * {@link petrolpark.mc.destroy.client.DestroyPartials}'s static block. Not intended for
 * external callers.
*/
    public void setPartial(PartialModel partial) {
        this.partial = partial;
    }

    public String getSymbol() {
        return symbol;
    }

    public Float getMass() {
        return mass;
    }

    public Float getElectronegativity() {
        return electronegativity;
    }

    public Boolean isValidValency(double valency) {
        for (double v : valencies) {
            if (Math.abs(v - valency) < 0.000001d) return true;
        }
        return false;
    }

    public double getNextLowestValency(double valency) {
        for (double validValency : valencies) {
            if (validValency >= valency) return validValency;
        }
        return 0;
    }

    public double getMaxValency() {
        double currentMax = valencies[0];
        for (double valency : valencies) {
            if (valency > currentMax) currentMax = valency;
        }
        return currentMax;
    }

    public static LegacyElement fromSymbol(String symbol) {
        for (LegacyElement element : values()) {
            if (element.symbol.equals(symbol)) return element;
        }
        throw new EnumConstantNotPresentException(LegacyElement.class, "Unknown Element of symbol " + symbol);
    }

    /**
 * Map the number of connections on an atom of this element to a {@link Geometry} for the
 * molecule renderer. Consults the per-element {@link #geometryOverride} first; if null or
 * returns null, falls back to the standard VSEPR-ish connection-count mapping:
 * <ul>
 * <li>0/1/2 connections → {@link Geometry#LINEAR}</li>
 * <li>3 connections → {@link Geometry#TRIGONAL_PLANAR}</li>
 * <li>4 connections → {@link Geometry#TETRAHEDRAL}</li>
 * <li>5/6 connections → {@link Geometry#OCTAHEDRAL} (5 is TODO for trigonal bipyramidal)</li>
 * </ul>
 *
 * <p><strong>Client-side only</strong>: calls this method trigger loading of
 * {@link petrolpark.mc.destroy.core.chemistry.MoleculeRenderer} which imports client-only
 * classes. Server code must not invoke this.</p>
*/
    public Geometry getGeometry(int connections) {
        Geometry geometry = null;
        if (geometryOverride != null) geometry = geometryOverride.apply(connections);
        if (geometry != null) return geometry;
        switch (connections) {
            case 0:
            case 1:
            case 2:
                return Geometry.LINEAR;
            case 3:
                return Geometry.TRIGONAL_PLANAR;
            case 4:
                return Geometry.TETRAHEDRAL;
            case 5:
                // TODO add trigonal bipyramidal geometry
            case 6:
                return Geometry.OCTAHEDRAL;
            default:
                return Geometry.OCTAHEDRAL;
        }
    }
}
