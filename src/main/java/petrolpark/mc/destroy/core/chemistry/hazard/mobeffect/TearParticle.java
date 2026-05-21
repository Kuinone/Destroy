package petrolpark.mc.destroy.core.chemistry.hazard.mobeffect;

import com.simibubi.create.content.equipment.bell.BasicParticleData;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import petrolpark.mc.destroy.client.DestroyParticleTypes;

/**
 * Small blue tear particle spawned by {@link CryingMobEffect} tick.
 *
 * <ul>
 * <li>Create {@code BasicParticleData} + {@code IBasicParticleFactory} — both kept in Create 6.0.9
 * (see {@code ConfettoParticleData} precedent)</li>
 * <li>{@code net.minecraft.client.particle.SpriteSet / TextureSheetParticle / ParticleRenderType} —
 * zero API changes in 1.21</li>
 * <li>{@code DestroyParticleTypes.TEAR} — added to the registry enum in the same session</li>
 * </ul>
*/
public class TearParticle extends TextureSheetParticle {

    public TearParticle(ClientLevel level, double x, double y, double z,
                         double vx, double vy, double vz,
                         SpriteSet spriteSet, ParticleOptions data) {
        super(level, x, y, z);

        setSize(0.01f, 0.01f);
        pickSprite(spriteSet);
        setColor(203 / 255f, 242 / 255f, 240 / 255f);
        xd = vx;
        yd = vy;
        zd = vz;
        gravity = 0.16f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Data extends BasicParticleData<TearParticle> {

        @Override
        public ParticleType<?> getType() {
            return DestroyParticleTypes.TEAR.get();
        }

        @Override
        public IBasicParticleFactory<TearParticle> getBasicFactory() {
            return (level, x, y, z, vx, vy, vz, spriteSet) -> new TearParticle(level, x, y, z, vx, vy, vz, spriteSet, this);
        }

        // Make all Data instances .equals each other so {@link
        // net.minecraft.network.codec.StreamCodec#unit(Object)} can encode any new instance against
        // the sentinel captured at particle-type registration. {@code BasicParticleData} inherits
        // {@code Object.equals} (reference equality); when we call {@code ServerLevel.sendParticles(
        // new TearParticle.Data(), ...)} the StreamCodec.unit's encode does
        // {@code newInstance.equals(sentinel)} which returns false → throws IllegalStateException
        // → particle packet never reaches client → tears invisible. This is a 1.21-specific gotcha
        // . Data has no
        // fields, so any two Data instances are semantically equivalent.
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Data;
        }

        @Override
        public int hashCode() {
            return Data.class.hashCode();
        }
    }
}
