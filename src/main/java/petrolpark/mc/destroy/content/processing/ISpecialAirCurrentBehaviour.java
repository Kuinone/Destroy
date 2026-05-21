package petrolpark.mc.destroy.content.processing;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;

/**
 * Marker interface for {@link TransportedItemStackHandlerBehaviour} subclasses that want a
 * custom hook into Create's Encased Fan {@link AirCurrent} tick — invoked each fan-affected
 * tick with the full AirCurrent (direction + pushing/pulling) and the current
 * {@link FanProcessingType} so the behaviour can decide whether to consume, reject, or
 * transform held items.
 *
 * @see petrolpark.mc.destroy.content.processing.glassblowing.BlowpipeBlockEntity.GlassblowingBehaviour
*/
public interface ISpecialAirCurrentBehaviour {

    void tickAir(AirCurrent airCurrent, FanProcessingType processingType);
}
