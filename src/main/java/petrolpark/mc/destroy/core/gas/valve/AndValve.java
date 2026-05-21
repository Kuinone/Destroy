package petrolpark.mc.destroy.core.gas.valve;

import java.util.stream.Stream;

/**
 * Series composition of multiple {@link IGasValve}s — flow must pass through all sequentially. Stream-flattens
 * nested AndValves at construction (so {@code AndValve.sequence(a, AndValve.sequence(b, c))}
 * = {@code AndValve.sequence(a, b, c)}).
*/
public class AndValve implements IGasValve {

    protected final IGasValve[] valves;

    protected AndValve(IGasValve... valves) {
        this.valves = valves;
    }

    public static IGasValve sequence(IGasValve... valves) {
        return new AndValve(Stream.of(valves).flatMap(valve -> {
            if (valve instanceof AndValve andValve) return Stream.of(andValve.valves);
            return Stream.of(valve);
        }).toArray(IGasValve[]::new));
    }

    @Override
    public double getPressureGradient(double inletPressure, double outletPressure) {
        // Series resistance formula likely
        // sum of inverse gradients then invert (pressure-dropper analogy).
        throw new UnsupportedOperationException("Unimplemented method 'getPressureGradient'");
    }
}
