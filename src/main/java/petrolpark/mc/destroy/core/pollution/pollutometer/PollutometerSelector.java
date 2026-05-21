package petrolpark.mc.destroy.core.pollution.pollutometer;

import java.util.function.Supplier;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.core.pollution.PollutionType;

/**
 * Adapter enum bridging Create's {@link com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour
 * ScrollOptionBehaviour}'s {@code <E extends Enum<E> & INamedIconOptions>} type bound to Destroy's
 * registry-based {@link PollutionType}. the 1.21 redesign made
 * {@code PollutionType<HOLDER>} a generic class registered through {@link DestroyPollutionTypes},
 * so we need this enum to satisfy the scroll behaviour's type constraints while still pointing at
 * the registered instances.
 *
 * <ul>
 * <li>A {@link Supplier} resolving to the registered {@code PollutionType<?>} (lazy because
 * the registry isn't populated when the enum class loads).</li>
 * <li>{@code chunkScoped} flag — true for {@code SMOG} and {@code RADIOACTIVITY}, needed to dispatch to the correct
 * {@link petrolpark.mc.destroy.core.pollution.PollutionHelper} overload at read time.</li>
 * <li>A Create {@link AllIcons} placeholder ({@code I_NONE}) — Destroy's pollution icons live in
 * {@link petrolpark.mc.destroy.client.DestroyIcon} which inherits from {@code PetrolparkIcon},
 * not Create's {@link AllIcons}; the scroll-option chip in Create's UI only uses the icon
 * when the option is rendered IN the value box, and the user-visible label is the
 * translation key, so {@code I_NONE} is acceptable until a richer icon mapping is added.</li>
 * </ul>
*/
public enum PollutometerSelector implements INamedIconOptions {

    GREENHOUSE     (() -> DestroyPollutionTypes.GREENHOUSE.get(),      false, AllIcons.I_NONE, "greenhouse"),
    OZONE_DEPLETION(() -> DestroyPollutionTypes.OZONE_DEPLETION.get(), false, AllIcons.I_NONE, "ozone_depletion"),
    ACID_RAIN      (() -> DestroyPollutionTypes.ACID_RAIN.get(),       false, AllIcons.I_NONE, "acid_rain"),
    SMOG           (() -> DestroyPollutionTypes.SMOG.get(),            true,  AllIcons.I_NONE, "smog"),
    RADIOACTIVITY  (() -> DestroyPollutionTypes.RADIOACTIVITY.get(),   true,  AllIcons.I_NONE, "radioactivity");

    @SuppressWarnings("rawtypes")
    private final Supplier<? extends PollutionType<?>> typeSupplier;
    /** True if this pollution type is per-chunk (SMOG / RADIOACTIVITY); false if per-level.*/
    public final boolean chunkScoped;
    private final AllIcons icon;
    private final String shortKey;

    @SuppressWarnings("rawtypes")
    PollutometerSelector(Supplier<? extends PollutionType<?>> typeSupplier, boolean chunkScoped,
                         AllIcons icon, String shortKey) {
        this.typeSupplier = typeSupplier;
        this.chunkScoped = chunkScoped;
        this.icon = icon;
        this.shortKey = shortKey;
    }

    /** Resolved {@link PollutionType} this selector points at; raw-typed because the holder
 * parametrisation differs per option (Level vs ChunkAccess). Callers can cast based on
 * {@link #chunkScoped}.*/
    @SuppressWarnings("unchecked")
    public PollutionType<Level> levelPollutionType() {
        return (PollutionType<Level>) typeSupplier.get();
    }

    @SuppressWarnings("unchecked")
    public PollutionType<ChunkAccess> chunkPollutionType() {
        return (PollutionType<ChunkAccess>) typeSupplier.get();
    }

    /** Untyped accessor; used by code that branches on {@link #chunkScoped}.*/
    public PollutionType<?> rawPollutionType() {
        return typeSupplier.get();
    }

    @Override
    public AllIcons getIcon() {
        return icon;
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pollutometer.pollution_type." + shortKey;
    }
}
