package petrolpark.mc.destroy.content.oil;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import petrolpark.mc.destroy.Destroy;

/**
 * Per-chunk crude oil deposit — deterministic Perlin-noise oil field map with random "red herring"
 * seismic signals mixed in to make prospecting non-trivial. Stored as a {@link net.neoforged.neoforge.attachment.AttachmentType}
 * on {@link net.minecraft.world.level.chunk.ChunkAccess} (see {@code DestroyAttachmentTypes.CHUNK_CRUDE_OIL}).
*/
public class ChunkCrudeOil {

    public static final long SALT = 5252525252L;

    public static final Codec<ChunkCrudeOil> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.BOOL.fieldOf("Generated").forGetter(c -> c.generated),
        Codec.INT.fieldOf("Amount").forGetter(c -> c.amount)
    ).apply(i, ChunkCrudeOil::new));

    private boolean generated; // Whether the amount of Crude Oil in this Chunk has already been determined
    private int amount; // Amount of Crude Oil in the Chunk in mB

    public ChunkCrudeOil() {
        this(false, 0);
    }

    public ChunkCrudeOil(boolean generated, int amount) {
        this.generated = generated;
        this.amount = amount;
    }

    /**
 * Get the amount of oil generated in this chunk. This does not account for if oil has been pumped out.
*/
    public static int getTheoreticalOil(ServerLevel level, int chunkX, int chunkZ) {
        RandomSource random = RandomSource.create(level.getSeed() ^ SALT);
        double value = (PerlinNoise.create(random, -2, 1d).getValue(chunkX * 1.5d, chunkZ * 1.5d, 0));
        return value < 0.3d ? 0 : (int)(value * 10000000d);
    }

    /**
 * Check whether a chunk would theoretically have any oil, regardless of whether its been pumped out.
*/
    public static boolean hasOil(ServerLevel level, int chunkX, int chunkZ) {
        return getTheoreticalOil(level, chunkX, chunkZ) > 0;
    }

    /**
 * To make prospecting less straightforward, some random chunks without oil also show as 'seismically active'.
 * Does not filter for false positives.
 *
 * @return {@code true} for roughly a quarter of all chunks
*/
    public static boolean randomSeismicActivity(ServerLevel level, int chunkX, int chunkZ) {
        RandomSource random = RandomSource.create(level.getSeed() ^ SALT ^ chunkX ^ chunkZ);
        random.nextInt();
        return random.nextInt(4) == 0;
    }

    private static boolean debug = false;

    /**
 * Map a chunk coordinate down to the nearest 8-chunk lower corner (matching seismograph
 * nonogram origin).
 *
 * <p>Formula: {@code mapChunkCenter = Mth.floor((coord + 4) / 8) * 8}; lower corner is center - 4.</p>
*/
    private static int mapChunkLowerCorner(int chunkCoordinate) {
        return Mth.floor((chunkCoordinate + 4d) / 8d) * 8 - 4;
    }

    /**
 * Get the 'signals' in a line (the "long axis") used for the seismograph nonogram.
 *
 * @param xNotZ {@code true} if the long axis is X, {@code false} if Z
 * @return A byte where each bit is {@code 1} if we show a signal on that chunk, starting on the
 * multiple of eight and ascending.
*/
    public static byte getSignals(ServerLevel level, int chunkX, int chunkZ, boolean xNotZ) {
        boolean[][] oil = new boolean[10][3];
        boolean[][] redHerring = new boolean[10][3];
        int widthAxis = xNotZ ? chunkZ : chunkX;
        int lengthAxis = xNotZ ? chunkX : chunkZ;
        for (int width = 0; width < 3; width++) {
            for (int length = 0; length < 10; length++) {
                int lengthCoordinate = mapChunkLowerCorner(lengthAxis) - 1 + length;
                int widthCoordinate = widthAxis - 1 + width;
                int x = xNotZ ? lengthCoordinate : widthCoordinate;
                int z = xNotZ ? widthCoordinate : lengthCoordinate;
                oil[length][width] = hasOil(level, x, z);
                redHerring[length][width] = randomSeismicActivity(level, x, z);
            }
        }
        byte signals = 0;
        for (int length = 1; length <= 8; length++) {
            boolean oilInSurroundings = false;
            boolean surroundedByHerrings = true;
            for (int lengthOffset = -1; lengthOffset <= 1; lengthOffset++) {
                checkAllSides: for (int widthOffset = -1; widthOffset <= 1; widthOffset++) {
                    if (lengthOffset != 0 && widthOffset != 0) continue checkAllSides;
                    if (oil[length + lengthOffset][1 + widthOffset]) oilInSurroundings = true;
                    if (!redHerring[length + lengthOffset][1 + widthOffset] && lengthOffset != 0 && widthOffset != 0) surroundedByHerrings = false;
                    if (oilInSurroundings) break checkAllSides;
                }
            }

            /*
             * If any oil is in us or adjacent, signal. If we're surrounded on 4 sides by red herrings,
             * avoid showing (false positive). Otherwise show red herring at this chunk.
             */
            if (oilInSurroundings || (!surroundedByHerrings && redHerring[length][1])) {
                signals |= (byte)(1 << (length - 1));
            }
        }
        if (debug) {
            Destroy.LOGGER.info("Oil: ");
            for (int i = 0; i <= 2; i++) {
                StringBuilder sb = new StringBuilder();
                for (boolean[] slice : oil) sb.append(slice[i] ? "O" : "_");
                Destroy.LOGGER.info((xNotZ ? " X " : " Z ") + sb);
            }
            Destroy.LOGGER.info("Herrings: ");
            for (int i = 0; i <= 2; i++) {
                StringBuilder sb = new StringBuilder();
                for (boolean[] slice : redHerring) sb.append(slice[i] ? "O" : "_");
                Destroy.LOGGER.info((xNotZ ? " X " : " Z ") + sb);
            }
            String string = Integer.toBinaryString(signals);
            string = string.substring(Math.max(0, string.length() - 8));
            Destroy.LOGGER.info((xNotZ ? " X " : " Z ") + "signals: " + string);
        }
        return signals;
    }

    public void generate(LevelChunk chunk, @Nullable Player player) {
        if (generated) return;
        if (chunk.getLevel() instanceof ServerLevel level) {
            ChunkPos pos = chunk.getPos();
            amount = getTheoreticalOil(level, pos.x, pos.z);
            // TODO: check for Player luck
            generated = true;
        }
    }

    public boolean isGenerated() {
        return generated;
    }

    public int getAmount() {
        return amount;
    }

    public int setAmount(int amount) {
        this.amount = Math.max(0, amount);
        return this.amount;
    }

    public int decreaseAmount(int decrease) {
        amount = (int) Math.max(0, amount - decrease);
        return amount;
    }
}
