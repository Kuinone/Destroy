package petrolpark.mc.destroy;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 1.21.1 notes:
*/
public class DestroyPotions {

    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Destroy.MOD_ID);

    public static final DeferredHolder<Potion, Potion> UNIQUE = POTIONS.register("unique", () -> new Potion((String) null, new MobEffectInstance[0]));

    public static void register(IEventBus bus) {
        POTIONS.register(bus);
    }
}
