package petrolpark.mc.destroy.core.pollution;

import java.util.Random;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.core.fluid.gasparticle.EvaporatingFluidS2CPacket;

/**
 * {@link OpenPipeEffectHandler} that triggers pollution tracking when an Open-Ended Pipe sprays
 * a pollutant fluid (Mixture or pollutant-tagged fluid).
*/
public class PollutingOpenEndedPipeEffectHandler implements OpenPipeEffectHandler {

    private static final Random random = new Random();

    @Override
    public void apply(Level level, AABB area, FluidStack fluid) {
        BlockPos pollutionAt = new BlockPos((int) area.minX, (int) area.maxY - 1, (int) area.minZ);
        PollutionHelper.pollute(level, pollutionAt, fluid);
        // evaporation particle sync restored now that gasparticle subdir is ported.
        // ~5% chance per tick to emit a particle burst.
        // Use sendToClientsAround for proximity-only sync (efficient for particle visual effect).
        // guard against empty fluid; NeoForge 1.21 default FluidStack codec rejects empty.
        if (!fluid.isEmpty() && level instanceof ServerLevel serverLevel && random.nextInt(20) == 0) {
            CatnipServices.NETWORK.sendToClientsAround(serverLevel, pollutionAt, 64d,
                new EvaporatingFluidS2CPacket(pollutionAt, fluid));
        }
    }
}
