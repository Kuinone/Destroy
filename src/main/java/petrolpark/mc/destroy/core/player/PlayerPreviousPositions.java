package petrolpark.mc.destroy.core.player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;

/**
 * Rolling queue of a player's recent block positions, sampled once per second. Used by
 * {@code ChorusWineItem} to pick a teleport target from a configurable number of seconds back.
*/
public class PlayerPreviousPositions {

    public static final int TICKS_PER_SECOND = 20;

    public static final Codec<PlayerPreviousPositions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.listOf().optionalFieldOf("positions", List.of())
            .forGetter(p -> List.copyOf(p.positions)),
        Codec.INT.optionalFieldOf("tick_counter", 0)
            .forGetter(p -> p.tickCounter)
    ).apply(instance, PlayerPreviousPositions::fromSaved));

    private final Deque<BlockPos> positions;
    private int tickCounter;

    public PlayerPreviousPositions() {
        this.positions = new ArrayDeque<>();
        this.tickCounter = 0;
    }

    private PlayerPreviousPositions(List<BlockPos> saved, int tickCounter) {
        this.positions = new ArrayDeque<>(saved);
        this.tickCounter = tickCounter;
    }

    /** Codec resurrection path — reifies the saved positions list into the internal deque.*/
    private static PlayerPreviousPositions fromSaved(List<BlockPos> positions, int tickCounter) {
        return new PlayerPreviousPositions(positions, tickCounter);
    }

    public BlockPos getOldestPosition() {
        return positions.peekFirst();
    }

    public void recordPosition(BlockPos pos, int maxSize) {
        positions.addLast(pos);
        while (positions.size() > maxSize) positions.pollFirst();
    }

    public void clearPositions() {
        positions.clear();
    }

    public void incrementTickCounter() {
        tickCounter++;
        if (tickCounter >= TICKS_PER_SECOND) tickCounter = 0;
    }

    /** True exactly once per second (when the counter wraps to 0 on this tick).*/
    public boolean hasBeenSecond() {
        return tickCounter == 0;
    }
}
