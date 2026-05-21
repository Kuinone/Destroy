package petrolpark.mc.destroy.core.gas;

/**
 * Stub implementation of {@link IGasVessel} representing the open atmosphere — the implicit
 * "outside" connection for any gas pipe network's terminal valve.
*/
public class Atmosphere implements IGasVessel {

    @Override
    public double getPressure() {
        // When the gas-pressure solver in GasNetwork is finished, return atmospheric pressure
        // (probably 1 atm = 101_325 Pa or whatever unit the rest of the gas system settles on).
        throw new UnsupportedOperationException("Unimplemented method 'getPressure'");
    }
}
