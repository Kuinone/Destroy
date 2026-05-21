package petrolpark.mc.destroy.core.gas.valve;

/**
 * Marker interface for any object that controls flow between two gas-network sections. The
 * pressure-gradient method is the read-side surface for the
 * {@link petrolpark.mc.destroy.core.gas.network.GasNetwork} solver.
 *
 * @since Destroy 0.1.2
 * @author petrolpark
*/
public interface IGasValve {

    double getPressureGradient(double inletPressure, double outletPressure);
}
