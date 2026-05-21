package petrolpark.mc.destroy.core.fluid;

import com.simibubi.create.content.equipment.bell.BasicParticleData;
import com.simibubi.create.content.equipment.bell.BasicParticleData.IBasicParticleFactory;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;

import petrolpark.mc.destroy.client.DestroyParticleTypes;

/**
 * Colored rain-drop particle — thin subclass of {@link TintedSplashParticle} used by
 * {@link petrolpark.mc.destroy.core.pollution.PollutionPonderScenes#acidRain acidRain Ponder scene}
 * and {@link petrolpark.mc.destroy.core.pollution.PollutionPonderScenes#reduction reduction}
 * scene to simulate chemically-tinted atmospheric precipitation.
*/
public class RainParticle extends TintedSplashParticle {

    public RainParticle(ClientLevel level, double x, double y, double z, double r, double g, double b, SpriteSet sprites) {
        super(level, x, y, z, r, g, b, sprites);
    }

    public static class Data extends BasicParticleData<RainParticle> {

        @Override
        public ParticleType<?> getType() {
            return DestroyParticleTypes.RAIN.get();
        }

        @Override
        public IBasicParticleFactory<RainParticle> getBasicFactory() {
            return RainParticle::new;
        }
    }
}
