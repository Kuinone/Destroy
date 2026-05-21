package petrolpark.mc.destroy.mixin.accessor;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;

/**
 * Mixin accessor exposing {@code BasinOperatingBlockEntity}'s protected
 * {@code getBasin()} helper so {@link
 * petrolpark.mc.destroy.mixin.MechanicalMixerBlockEntityMixin} can resolve the basin associated
 * with the mixer when injecting reaction-recipe matches into {@code getMatchingRecipes()}.
*/
@Mixin(BasinOperatingBlockEntity.class)
public interface BasinOperatingBlockEntityAccessor {

    @Invoker(value = "getBasin", remap = false)
    Optional<BasinBlockEntity> invokeGetBasin();
}
