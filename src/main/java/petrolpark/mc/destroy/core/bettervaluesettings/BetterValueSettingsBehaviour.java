package petrolpark.mc.destroy.core.bettervaluesettings;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;

/**
 * Extension of Create's {@link ValueSettingsBehaviour} that adds a callback for the access face
 * + interaction hand — needed by behaviours like {@link SidedScrollValueBehaviour} that store
 * per-side values and need to know which side the player just clicked on.
*/
public interface BetterValueSettingsBehaviour extends ValueSettingsBehaviour {

    /**
 * Allow this Value Box behaviour to accept additional information when its value is set.
 * @param interactionHand The hand used to set the value
 * @param face The face of the Block on which the value was set
*/
    default void acceptAccessInformation(InteractionHand interactionHand, Direction face) {}
}
