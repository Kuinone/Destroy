package petrolpark.mc.destroy;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Destroy's own {@link DataComponentType} registry.
 *
 * <p>Registered from {@link Destroy#Destroy(IEventBus, net.neoforged.fml.ModContainer)} via
 * {@link #register(IEventBus)}.</p>
*/
public class DestroyDataComponents {

    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Destroy.MOD_ID);

    /**
 * The Mixture payload attached to a {@code FluidStack} of
 * {@link DestroyFluids#MIXTURE} or {@link DestroyFluids#GAS_MIXTURE}. Payload is the Mixture's
 * NBT as written by {@code ReadOnlyMixture.writeNBT()} / read by
 * {@code ReadOnlyMixture.readNBT(Supplier, CompoundTag)}.
*/
    public static final DataComponentType<CompoundTag> MIXTURE = register("mixture",
        b -> b.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    /**
 *
 * <p>Tag keys:</p>
 * <ul>
 * <li>{@code Subtype} (String) — "molecule" / "molecule_tag" / "ion" / "salt" / "pure"</li>
 * <li>{@code Id} (String) — molecule id / tag id / cation id (subtype-specific)</li>
 * <li>{@code Id2} (String, optional) — anion id (only for SaltFluidIngredient)</li>
 * <li>{@code MinConcentration} (Float)</li>
 * <li>{@code MaxConcentration} (Float)</li>
 * <li>{@code Anion} (Boolean, optional) — for ion subtype only</li>
 * </ul>
*/
    public static final DataComponentType<CompoundTag> MIXTURE_INGREDIENT_INFO = register("mixture_ingredient_info",
        b -> b.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    /**
 * A FluidStack attached to an ItemStack marking it as "contaminated" with a hazardous chemical
 * Mixture. Consumed by {@code ChemistryHazardHelper.damage(...)} when the Player unequips the
 * armor without washing it first — replays the Mixture exposure with full damage.
*/
    public static final DataComponentType<FluidStack> CONTAMINATING_FLUID = register("contaminating_fluid",
        b -> b.persistent(FluidStack.CODEC).networkSynchronized(FluidStack.STREAM_CODEC));

    /**
 * Fluid-tank contents for MixtureStorageItems (TEST_TUBE · BALLOON · future MeasuringCylinder etc.).
 * Uses NeoForge's {@link net.neoforged.neoforge.fluids.SimpleFluidContent} record — stable
 * serialization for 1 fluidstack slot · compatible with {@link net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack}.
*/
    public static final DataComponentType<net.neoforged.neoforge.fluids.SimpleFluidContent> MIXTURE_TANK = register("mixture_tank",
        b -> b.persistent(net.neoforged.neoforge.fluids.SimpleFluidContent.CODEC)
              .networkSynchronized(net.neoforged.neoforge.fluids.SimpleFluidContent.STREAM_CODEC));

    /**
 * "Injecting" flag on a {@code SyringeItem} stack — set while the player holds use to stop
 * {@link net.minecraft.world.item.ItemStack#getUseDuration} from being observably different
 * between pressing and holding. A presence-only flag; {@code Boolean.TRUE}
 * is the only stored value.
*/
    public static final DataComponentType<Boolean> INJECTING = register("injecting",
        b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    /**
 * Which tool (ordinal of {@code SwissArmyKnifeItem.Tool}) is currently active on the
 * {@code SwissArmyKnifeItem} stack.
*/
    public static final DataComponentType<Integer> ACTIVE_TOOL = register("active_tool",
        b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
 * Float 0..1 identifying which sub-model the SwissArmyKnifeItemRenderer should render in the
 * current frame. Only consumed by the renderer's per-tool layer loop; no gameplay meaning.
*/
    public static final DataComponentType<Float> RENDERED_TOOL = register("rendered_tool",
        b -> b.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));

    /**
 * The Seismograph state payload (rowsDiscovered byte, columnsDiscovered byte, rows[8], columns[8],
 * marks[64]) attached to a {@code SeismographItem} stack.
*/
    public static final DataComponentType<CompoundTag> SEISMOGRAPH = register("seismograph",
        b -> b.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    /**
 * 16-bit binary matrix (4×4 grid packed as a single int, each bit one cell) representing a
 * circuit pattern on a {@code CircuitPatternItem} or {@code SequencedAssemblyItem} in the
 * trypolithography (circuit fabrication) pipeline. Uses plain {@code DataComponentType<Integer>} since
 * the pattern is already a compact int wrapper (no nested structure needed).
*/
    public static final DataComponentType<Integer> CIRCUIT_PATTERN = register("circuit_pattern",
        b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
 * List of keypunch block UUIDs that have previously punched a {@code CircuitMaskItem}. When
 * the list reaches 3 entries, the mask becomes RUINED (can no longer be reused).
*/
    public static final DataComponentType<List<UUID>> PUNCHED_BY = register("punched_by",
        b -> b.persistent(Codec.list(UUIDUtil.CODEC))
              .networkSynchronized(ByteBufCodecs.fromCodec(Codec.list(UUIDUtil.CODEC))));

    /**
 * Flag set on a {@code CircuitMaskItem} stack when it's in-flight after being ejected by a
 * Weighted Ejector — tells the renderer to draw the mask flipped 180° around the north-south
 * axis (pre-rotation) so the eye-side of the mask faces up correctly. Cleared during
 * {@code inventoryTick} when the mask lands back in an inventory.
*/
    public static final DataComponentType<Boolean> FLIPPED = register("flipped",
        b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    /**
 * Flag on a {@code CircuitMaskItem} stack that suppresses the "Punched by X" contaminant list
 * from the hover tooltip. Set by datapacks / creative items that don't need the tracking —
 * gameplay masks always leave this unset.
*/
    public static final DataComponentType<Boolean> HIDE_CONTAMINANTS = register("hide_contaminants",
        b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    /**
 * Per-item UUID for a {@code RedstoneProgrammerBlockItem} stack — keys into
 * {@code RedstoneProgrammerItemHandler.programs} so the server-side tick processor can
 * identify which Program belongs to which stack.
*/
    public static final DataComponentType<UUID> PROGRAMMER_UUID = register("programmer_uuid",
        b -> b.persistent(UUIDUtil.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(UUIDUtil.CODEC)));

    /**
 * Serialized {@link petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgram} state
 * stored on a {@code RedstoneProgrammerBlockItem} stack.
*/
    public static final DataComponentType<CompoundTag> PROGRAMMER_PROGRAM = register("programmer_program",
        b -> b.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    /**
 * The music disc stamped into a {@code DiscStamperItem} stack. the default value is {@link ItemStack#EMPTY}.
*/
    public static final DataComponentType<ItemStack> STAMPED_DISC = register("stamped_disc",
        b -> b.persistent(ItemStack.OPTIONAL_CODEC).networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC));

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // Blowing / Progress / LastProgress / Tank / Recipe / RequiredFluid.
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    /** Is the Blowpipe currently being blown into by its holder? Drives render-layer blow pose.*/
    public static final DataComponentType<Boolean> BLOWPIPE_BLOWING = register("blowpipe_blowing",
        b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    /** Blowing progress (0..{@code BlowpipeBlockEntity.BLOWING_DURATION}).*/
    public static final DataComponentType<Integer> BLOWPIPE_PROGRESS = register("blowpipe_progress",
        b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Previous-tick snapshot of Progress, for interp'd renderer animation.*/
    public static final DataComponentType<Integer> BLOWPIPE_LAST_PROGRESS = register("blowpipe_last_progress",
        b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Blowpipe's internal FluidTank contents (molten glass etc). Defaults to EMPTY; uses FluidStack.OPTIONAL_CODEC/STREAM_CODEC.*/
    public static final DataComponentType<FluidStack> BLOWPIPE_TANK = register("blowpipe_tank",
        b -> b.persistent(FluidStack.OPTIONAL_CODEC).networkSynchronized(FluidStack.OPTIONAL_STREAM_CODEC));

    /** Selected GlassblowingRecipe's ResourceLocation.*/
    public static final DataComponentType<ResourceLocation> BLOWPIPE_RECIPE = register("blowpipe_recipe",
        b -> b.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC));

    /** Required fluid for the selected GlassblowingRecipe — stored directly as a
 * {@link SizedFluidIngredient}.*/
    public static final DataComponentType<SizedFluidIngredient> BLOWPIPE_REQUIRED_FLUID = register("blowpipe_required_fluid",
        b -> b.persistent(SizedFluidIngredient.FLAT_CODEC).networkSynchronized(SizedFluidIngredient.STREAM_CODEC));

    /** Mixed-explosive inventory payload on {@code IMixedExplosiveItem} stacks.*/
    public static final DataComponentType<CompoundTag> EXPLOSIVE_MIX = register("explosive_mix",
        b -> b.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    /**
 * Molecule full-ID string attached to a {@code MOLECULE_DISPLAY} ItemStack. 21 typed String DataComponent. The payload is the same
 * {@code LegacySpecies.getFullID()} output used by {@code LegacySpecies.getMolecule(String)}
 * for lookup. Registered for {@link petrolpark.mc.destroy.core.chemistry.MoleculeDisplayItem
 * MoleculeDisplayItem}.
*/
    public static final DataComponentType<String> MOLECULE = register("molecule",
        b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.<T>builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
