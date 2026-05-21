package petrolpark.mc.destroy.content.oil.seismology;

import java.util.Arrays;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyDataComponents;

/**
 * Prospector's Seismograph — extends vanilla {@link MapItem}; overlays a per-chunk nonogram
 * encoding the "seismic signals" (see {@link petrolpark.mc.destroy.content.oil.ChunkCrudeOil#getSignals}
 * ChunkCrudeOil.getSignals}) so the player can reason about where the underlying oil reservoirs are
 * hidden among "red herring" signals. Stored state: an 8×8 grid of player-marks (TICK / CROSS /
 * GUESSED_TICK / GUESSED_CROSS) + row/column 1D signal bytes + "discovered" bit masks.
 *
 * <p><strong>Deferred (need follow-up sessions)</strong>:</p>
*/
public class SeismographItem extends MapItem {

    public SeismographItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide && net.neoforged.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
            ClientScreenOpener.open(stack, usedHand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** The previous
 * {@code @OnlyIn(Dist.CLIENT)} on a separate {@code openScreen} method may not protect against
 * RuntimeDistCleaner edge cases — the nested-class pattern is unambiguously safe (lazy class
 * resolution).
*/
    public static final class ClientScreenOpener {
        private ClientScreenOpener() {}
        public static void open(ItemStack stack, InteractionHand hand) {
            ScreenOpener.open(new SeismographScreen(stack, hand));
        }
    }

    public static Seismograph readSeismograph(ItemStack stack) {
        Seismograph seismograph = new Seismograph();
        if (!(stack.getItem() instanceof SeismographItem)) return seismograph;
        CompoundTag tag = stack.getOrDefault(DestroyDataComponents.SEISMOGRAPH, new CompoundTag());
        seismograph.rowsDiscovered = tag.getByte("RowsDiscovered");
        seismograph.columnsDiscovered = tag.getByte("ColumnsDiscovered");
        seismograph.rows = Arrays.copyOf(tag.getByteArray("Rows"), 8);
        seismograph.columns = Arrays.copyOf(tag.getByteArray("Columns"), 8);
        seismograph.marks = Arrays.copyOf(tag.getByteArray("Markings"), 64);
        return seismograph;
    }

    public static void writeSeismograph(ItemStack stack, Seismograph seismograph) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("RowsDiscovered", seismograph.rowsDiscovered);
        tag.putByte("ColumnsDiscovered", seismograph.columnsDiscovered);
        tag.putByteArray("Rows", seismograph.rows);
        tag.putByteArray("Columns", seismograph.columns);
        tag.putByteArray("Markings", seismograph.marks);
        stack.set(DestroyDataComponents.SEISMOGRAPH, tag);
    }

    public static class Seismograph {

        private byte rowsDiscovered;
        private byte columnsDiscovered;
        private byte[] rows;
        private byte[] columns;
        private byte[] marks;

        public Seismograph() {
            rows = new byte[8];
            columns = new byte[8];
            marks = new byte[64];
        }

        public void clear() {
            rowsDiscovered = 0;
            columnsDiscovered = 0;
            rows = new byte[8];
            columns = new byte[8];
            marks = new byte[64];
        }

        public byte[] getRows() {
            return rows;
        }

        public byte[] getColumns() {
            return columns;
        }

        public byte[] getMarks() {
            return marks;
        }

        public Mark getMark(int x, int z) {
            if (x < 0 || x > 7 || z < 0 || z > 7) return Mark.NONE;
            return Mark.values()[marks[x * 8 + z]];
        }

        /**
 * @return Whether the mark changed
*/
        public boolean mark(int x, int z, Mark mark) {
            if (x < 0 || x > 7 || z < 0 || z > 7) return false;
            boolean changed = marks[x * 8 + z] != (byte) mark.ordinal();
            marks[x * 8 + z] = (byte) mark.ordinal();
            return changed;
        }

        public int[] getColumnDisplayed(int column) {
            return getDisplayed(columns, column);
        }

        public int[] getRowDisplayed(int row) {
            return getDisplayed(rows, row);
        }

        public int[] getDisplayed(byte[] array, int index) {
            if (index < 0 || index > 7) return new int[0];
            int[] numbers = new int[4];
            int numbersAdded = 0;
            int count = 0;
            for (int i = 0; i <= 8; i++) { // one extra iteration so the final count gets appended
                if (i != 8 && (array[index] & (1 << i)) != 0) {
                    count++;
                } else if (count > 0) {
                    numbers[numbersAdded] = count;
                    numbersAdded++;
                    count = 0;
                }
            }
            return numbers;
        }

        /**
 * Let the Seismograph know we've collected data and filled in this row
 *
 * @return {@code true} if that row had not been filled in already
*/
        public boolean discoverRow(int row, Level level, Player player) {
            if (row < 0 || row > 7) return false;
            byte oldRowsDiscovered = rowsDiscovered;
            rowsDiscovered |= (byte) (1 << row);
            triggerFillSeismographAdvancement(level, player);
            return oldRowsDiscovered != rowsDiscovered;
        }

        /**
 * Let the Seismograph know we've collected data and filled in this column
 *
 * @return {@code true} if that column had not been filled in already
*/
        public boolean discoverColumn(int column, Level level, Player player) {
            if (column < 0 || column > 7) return false;
            byte oldColumnsDiscovered = columnsDiscovered;
            columnsDiscovered |= (byte) (1 << column);
            triggerSolveSeismographAdvancement(level, player);
            return oldColumnsDiscovered != columnsDiscovered;
        }

        public boolean isRowDiscovered(int row) {
            if (row < 0 || row > 7) return false;
            return (rowsDiscovered & 1 << row) != 0;
        }

        public boolean isColumnDiscovered(int column) {
            if (column < 0 || column > 7) return false;
            return (columnsDiscovered & 1 << column) != 0;
        }

        /**
 * Check if all columns and rows have been discovered; if so, award the
 * {@link DestroyAdvancementTrigger#FILL_SEISMOGRAPH} advancement.
 *
 * @return {@code true} if all columns and rows have been discovered, whether or not the
 * player already has the advancement
*/
        private boolean triggerFillSeismographAdvancement(Level level, Player player) {
            if (rowsDiscovered == -1 && columnsDiscovered == -1) {
                if (level != null && player != null) DestroyAdvancementTrigger.FILL_SEISMOGRAPH.award(level, player);
                return true;
            }
            return false;
        }

        public void triggerSolveSeismographAdvancement(Level level, Player player) {
            if (triggerFillSeismographAdvancement(level, player)) {
                for (boolean test : Iterate.trueAndFalse) {
                    for (int x = 0; x < 8; x++) {
                        for (int z = 0; z < 8; z++) {
                            boolean shouldBeTicked = (rows[z] & 1 << x) != 0;
                            if (test) {
                                Mark mark = getMark(x, z);
                                if (mark == Mark.NONE) return;
                                if ((mark == Mark.TICK || mark == Mark.GUESSED_TICK) != shouldBeTicked) return;
                            } else {
                                mark(x, z, shouldBeTicked ? Mark.TICK : Mark.CROSS);
                            }
                        }
                    }
                }
                if (level != null && player != null) DestroyAdvancementTrigger.COMPLETE_SEISMOGRAPH.award(level, player);
            }
        }

        /**
 * Player-mark enum for the nonogram grid. Ordinal values are stable across versions —
 * they are what get persisted in the {@code Markings} byte array.
*/
        public enum Mark {
            NONE,
            TICK,
            CROSS,
            GUESSED_TICK,
            GUESSED_CROSS;
        }
    }

    public static int mapChunkCenter(int chunkCoordinate) {
        return Mth.floor((chunkCoordinate + 4d) / 8d) * 8;
    }

    public static int mapChunkLowerCorner(int chunkCoordinate) {
        return mapChunkCenter(chunkCoordinate) - 4;
    }

    // TODO: restore onRenderItemInFrame handler once
    // SeismographItemRenderer and DestroyGuiTextures are ported.
    // net.minecraftforge.client.event.RenderItemInFrameEvent — 1.21 equivalent is at
    // net.neoforged.neoforge.client.event.RenderItemInFrameEvent.
    // Until restored, frame-mounted Seismographs render as default maps.
    @SuppressWarnings("unused")
    private static void onRenderItemInFrame_deferred() {}
}
