package petrolpark.mc.destroy.chemistry.api.util;

/**
 * Universal physical + unit-conversion constants used by Destroy's chemistry engine.
*/
public class Constants {

    /** Avogadro's Constant.*/
    public static final double AVAGADRO_CONSTANT = 6.02214076e23d;

    /** The Boltzman Constant, in joules per kelvin.*/
    public static final double BOLTZMAN_CONSTANT = 1.380649e-23d;

    /** The Ideal Gas Constant, in joules per kelvin per mole.*/
    public static final double GAS_CONSTANT = AVAGADRO_CONSTANT * BOLTZMAN_CONSTANT;

    /** The number of Minecraft millibuckets (mB) equal to one liter.*/
    public static final float MILLIBUCKETS_PER_LITER = 1f;
}
