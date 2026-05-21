package petrolpark.mc.destroy;

import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.util.DeferredSoundType;

public class DestroySoundTypes {

    public static final DeferredSoundType COOLER = new DeferredSoundType(1, 1,
        () -> DestroySoundEvents.COOLER_BREAK.getMainEvent(),
        () -> SoundEvents.STONE_STEP,
        () -> DestroySoundEvents.COOLER_PLACE.getMainEvent(),
        () -> SoundEvents.STONE_HIT,
        () -> SoundEvents.STONE_FALL
    );
}
