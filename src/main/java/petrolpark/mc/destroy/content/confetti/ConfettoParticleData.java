package petrolpark.mc.destroy.content.confetti;

import com.simibubi.create.content.equipment.bell.BasicParticleData;

import net.minecraft.core.particles.ParticleType;
import petrolpark.mc.destroy.client.DestroyParticleTypes;

/**
 * {@link ParticleType} options for confetto. Two variants (default + white) mapped to different
 * registered particle types.
*/
public class ConfettoParticleData extends BasicParticleData<ConfettoParticle> {

    @Override
    public ParticleType<?> getType() {
        return DestroyParticleTypes.CONFETTO.get();
    }

    @Override
    public IBasicParticleFactory<ConfettoParticle> getBasicFactory() {
        return ConfettoParticle::new;
    }

    public static class White extends ConfettoParticleData {

        @Override
        public ParticleType<?> getType() {
            return DestroyParticleTypes.WHITE_CONFETTO.get();
        }
    }
}
