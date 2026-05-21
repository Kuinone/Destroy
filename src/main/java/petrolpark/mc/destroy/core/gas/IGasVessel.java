package petrolpark.mc.destroy.core.gas;

/**
 * Marker interface for any block / object that holds a gas at some pressure (Atmosphere, future
 * gas tanks, gas pipes, etc.). The pressure value is the read-side surface for the
 * {@link petrolpark.mc.destroy.core.gas.network.GasNetwork} solver.
*/
public interface IGasVessel {

    double getPressure();
}
