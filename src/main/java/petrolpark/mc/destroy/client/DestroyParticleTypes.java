package petrolpark.mc.destroy.client;

import java.util.function.Supplier;

import com.simibubi.create.foundation.particle.ICustomParticleData;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.content.confetti.ConfettoParticleData;
import petrolpark.mc.destroy.core.chemistry.hazard.mobeffect.TearParticle;

/**
 * Destroy 的 {@link ParticleType} 注册表。模板参照 {@code com.simibubi.create.AllParticleTypes}——
 * enum 驱动 + 内嵌 {@code ParticleEntry} 用 NeoForge 1.21 的 {@code DeferredRegister} + {@code DeferredHolder}。
 *
 * <p>本 session 只注册 {@code CONFETTO / WHITE_CONFETTO}；将来 DISTILLATION / EVAPORATION / RAIN / TEAR /
 * TINTED_SPLASH / BOILING_FLUID_BUBBLE 加一行 enum 常量即可。</p>
*/
public enum DestroyParticleTypes {

    CONFETTO(ConfettoParticleData::new),
    WHITE_CONFETTO(ConfettoParticleData.White::new),
    TEAR(TearParticle.Data::new),
    // gasparticle subdir port. 3 new particle types.
    DISTILLATION(petrolpark.mc.destroy.core.fluid.gasparticle.GasParticleData::new),
    EVAPORATION(petrolpark.mc.destroy.core.fluid.gasparticle.GasParticleData::new),
    BOILING_FLUID_BUBBLE(petrolpark.mc.destroy.core.fluid.gasparticle.BoilingFluidBubbleParticleData::new),
    // core/fluid tinted rain particle pair. Unblocks PollutionPonderScenes.acidRain.
    TINTED_SPLASH(petrolpark.mc.destroy.core.fluid.TintedSplashParticle.Data::new),
    RAIN(petrolpark.mc.destroy.core.fluid.RainParticle.Data::new),
    ;

    private final ParticleEntry<?> entry;

    <T extends ParticleOptions> DestroyParticleTypes(Supplier<? extends ICustomParticleData<T>> typeProvider) {
        this.entry = new ParticleEntry<>(Lang.asId(name()), typeProvider);
    }

    public ParticleType<?> get() {
        return entry.object.get();
    }

    public static void register(IEventBus modEventBus) {
        ParticleEntry.REGISTER.register(modEventBus);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerFactories(RegisterParticleProvidersEvent event) {
        for (DestroyParticleTypes particle : values()) particle.entry.registerFactory(event);
    }

    private static class ParticleEntry<T extends ParticleOptions> {
        private static final DeferredRegister<ParticleType<?>> REGISTER =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Destroy.MOD_ID);

        private final String name;
        private final Supplier<? extends ICustomParticleData<T>> typeProvider;
        private final DeferredHolder<ParticleType<?>, ParticleType<T>> object;

        ParticleEntry(String name, Supplier<? extends ICustomParticleData<T>> typeProvider) {
            this.name = name;
            this.typeProvider = typeProvider;
            this.object = REGISTER.register(name, () -> typeProvider.get().createType());
        }

        @OnlyIn(Dist.CLIENT)
        public void registerFactory(RegisterParticleProvidersEvent event) {
            typeProvider.get().register(object.get(), event);
        }
    }
}
