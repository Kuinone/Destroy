package petrolpark.mc.destroy.content.oil.pumpjack;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.DestroyBlocks;

/**
 * Cam BlockEntity for the Pumpjack — tracks which PumpjackBlockEntity owns it (via
 * {@link #pumpjackPos} relative offset) so only the owner's controller can pull rotation from
 * this cam. 3-tick warmup before accepting pumping power (lets the multi-block structure
 * settle after placement).
*/
public class PumpjackCamBlockEntity extends KineticBlockEntity {

    /** Relative offset from the PumpjackBlock controller (if any) that is currently powering this cam.*/
    public BlockPos pumpjackPos;
    private int initialTicks;

    public PumpjackCamBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        initialTicks = 3;
    }

    @Override
    public void tick() {
        super.tick();
        if (initialTicks > 0) {
            initialTicks--;
        }
    }

    @Override
    protected Block getStressConfigKey() {
        return DestroyBlocks.PUMPJACK.get();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        initialTicks = compound.getInt("Warmup");
        if (compound.contains("PumpjackPos", Tag.TAG_COMPOUND)) {
            pumpjackPos = NbtUtils.readBlockPos(compound, "PumpjackPos").orElse(null);
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("Warmup", initialTicks);
        if (pumpjackPos != null) {
            compound.put("PumpjackPos", NbtUtils.writeBlockPos(pumpjackPos));
        }
    }

    public void update(BlockPos sourcePos) {
        pumpjackPos = getBlockPos().subtract(sourcePos);
    }

    public void remove(BlockPos sourcePos) {
        if (!isPowering(sourcePos)) return;
        pumpjackPos = null;
    }

    public boolean canPower(BlockPos globalPos) {
        return initialTicks == 0 && (pumpjackPos == null || isPowering(globalPos));
    }

    public boolean isPowering(BlockPos globalPos) {
        BlockPos key = getBlockPos().subtract(globalPos);
        return key.equals(pumpjackPos);
    }
}
