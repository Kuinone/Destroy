package petrolpark.mc.destroy.content.confetti;

import java.util.function.Supplier;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** The particle colour variant is chosen via the {@code particleFactory}
 * supplier wired at registration (CONFETTI = {@link ConfettoParticleData}::new, WHITE_CONFETTI = {@link ConfettoParticleData.White}::new).
*/
public class ConfettiItem extends Item {

    public final Supplier<ParticleOptions> particleFactory;

    public ConfettiItem(Properties properties, Supplier<ParticleOptions> particleFactory) {
        super(properties);
        this.particleFactory = particleFactory;
        DispenserBlock.registerBehavior(this, new ConfettiDispenserBehaviour());
    }

    public class ConfettiDispenserBehaviour extends OptionalDispenseItemBehavior {

        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            Direction facing = source.state().getValue(DispenserBlock.FACING);
            Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
            Vec3 pos = Vec3.atCenterOf(source.pos()).add(normal.scale(0.5f));
            Vec3 velocity = normal.scale(0.25f);
            if (source.level() instanceof ServerLevel serverLevel) {
                // pass `stack.copyWithCount(1)` not `stack` to the packet. The packet
                // captures the reference and is encoded asynchronously on the network thread; if
                // we then call `stack.shrink(1)` below and the stack had count=1, the original
                // ItemStack mutates to EMPTY before encoding runs, and 1.21's `ItemStack.STREAM_CODEC`
                // throws "Empty ItemStack not allowed" on encode. The receiver only needs the item type
                // (ConfettiItem) for the particleFactory, count doesn't matter.
                PacketDistributor.sendToPlayersNear(serverLevel, null,
                    pos.x, pos.y, pos.z, 32.0,
                    new ConfettiBurstS2CPacket(stack.copyWithCount(1), pos, velocity));
            }
            stack.shrink(1);
            setSuccess(true);
            return stack;
        }
    }
}
