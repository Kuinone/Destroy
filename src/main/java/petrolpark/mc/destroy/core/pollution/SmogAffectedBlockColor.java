package petrolpark.mc.destroy.core.pollution;

import java.util.function.BiFunction;
import java.util.function.IntSupplier;

import javax.annotation.Nullable;

import net.createmod.catnip.theme.Color;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyPollutionTypes;

/**
 * BlockColor replacement for tinted blocks (grass / foliage / water / etc.) that darkens their
 * tint by smog level. Darker smog → more brown mixed in.
 *
 * <ul>
 * <li>{@code Pollution.CAPABILITY + LazyOptional<Pollution>} → direct
 * {@code chunk.getData(DestroyAttachmentTypes.CHUNK_POLLUTION)} path. SMOG is chunk-scoped in the 1.21 port (see DestroyPollutionTypes).</li>
 * <li>{@code PollutionType.SMOG.max} (enum field) → {@code PollutionHelper.getPollutionProportion}
 * returning a normalized 0..1 float directly — SMOG max comes from the DataMap-based
 * {@code PollutionType.Properties}, no enum field access</li>
 * <li>{@code Biome::getGrassColor} and ColorResolver bifunction approach unchanged</li>
 * </ul>
*/
public class SmogAffectedBlockColor implements BlockColor {

    // use vanilla BiomeColors resolvers (pre-registered in ClientLevel.tintCaches). Custom
    // unregistered ColorResolver lambdas would throw NPE during chunk tesselation because
    // ClientLevel has no BlockTintCache entry for them in 1.21 (tintCaches map no longer
    // auto-grows on unknown resolvers — must be registered via RegisterColorHandlersEvent
    // .ColorResolvers or reuse vanilla ones).
    public static final int getAverageGrassColor(BlockAndTintGetter level, BlockPos pos) {
        return BiomeColors.getAverageGrassColor(level, pos);
    }

    public static final int getAverageFoliageColor(BlockAndTintGetter level, BlockPos pos) {
        return BiomeColors.getAverageFoliageColor(level, pos);
    }

    public static final int getAverageWaterColor(BlockAndTintGetter level, BlockPos pos) {
        return BiomeColors.getAverageWaterColor(level, pos);
    }

    public static final BlockColor
        GRASS             = new SmogAffectedBlockColor(SmogAffectedBlockColor::getAverageGrassColor,  GrassColor::getDefaultColor),
        DOUBLE_TALL_GRASS = new SmogAffectedBlockColor(SmogAffectedBlockColor::getAverageGrassColor,  GrassColor::getDefaultColor),
        PINK_PETALS       = new SmogAffectedBlockColor(SmogAffectedBlockColor::getAverageGrassColor,  GrassColor::getDefaultColor),
        FOLIAGE           = new SmogAffectedBlockColor(SmogAffectedBlockColor::getAverageFoliageColor, FoliageColor::getDefaultColor),
        BIRCH             = new SmogAffectedBlockColor(SmogAffectedBlockColor::getAverageFoliageColor, FoliageColor::getBirchColor),
        SPRUCE            = new SmogAffectedBlockColor(SmogAffectedBlockColor::getAverageFoliageColor, FoliageColor::getEvergreenColor),
        WATER             = new SmogAffectedBlockColor(SmogAffectedBlockColor::getAverageWaterColor,  () -> 4159204),
        SUGAR_CANE        = new SmogAffectedBlockColor(SmogAffectedBlockColor::getAverageGrassColor,  () -> -1);

    private final IntSupplier fallback;
    private final BlockColor wrapped;

    public SmogAffectedBlockColor(BiFunction<BlockAndTintGetter, BlockPos, Integer> levelAndPosBlockColor, IntSupplier fallback) {
        this((state, level, pos, tintIndex) -> level != null && pos != null
            ? levelAndPosBlockColor.apply(level, pos)
            : fallback.getAsInt(), fallback);
    }

    public SmogAffectedBlockColor(BlockColor wrapped, IntSupplier fallback) {
        this.wrapped = wrapped;
        this.fallback = fallback;
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        // PonderLevel special-case: the ponder scene renders with a synthetic level, so there's no
        // real chunk/pollution.
        // Pollution.CAPABILITY)` which always returned empty in ponder. In 1.21 there's no
        // guaranteed Pollution attachment on ponder levels, so we just use the unsmogged fallback.
        if (level instanceof PonderLevel) return fallback.getAsInt();
        return wrapped.getColor(state, level, pos, tintIndex);
    }

    /**
 * Get the color of a Block due to Smog, not accounting for the colors of any Blocks surrounding it.
*/
    public static int getColor(int originalColor, @Nullable BlockPos pos, BlockAndTintGetter level) {
        if (pos != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return originalColor;
            ChunkPos chunkPos = new ChunkPos(pos);
            LevelChunk chunk = mc.level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false);
            if (chunk != null) return getColor(originalColor, chunk);
        }
        return originalColor;
    }

    /**
 * 1.21 adaptation: takes a {@link ChunkAccess} directly. Reads the chunk's CHUNK_POLLUTION attachment and
 * mixes the brown-smog color proportional to SMOG level.
*/
    public static int getColor(int originalColor, ChunkAccess chunk) {
        ChunkPollution pollution = chunk.getData(DestroyAttachmentTypes.CHUNK_POLLUTION);
        float smogProportion = PollutionHelper.getPollutionProportion(chunk, DestroyPollutionTypes.SMOG.get());
        // Keep pollution reference to be explicit that this path reads attachment state (defensive
        // against the attachment lookup being optimized away in future refactors).
        if (pollution == null) return originalColor;
        return Color.mixColors(originalColor, brown, smogProportion);
    }

    private static final int brown = 0xFF3F332A;
}
