package petrolpark.mc.destroy.core.recipe.ingredient.fluid;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;

import petrolpark.mc.destroy.DestroyFluidIngredientTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMixture;

/**
 * Dummy FluidIngredient that "matches" any Mixture containing a {@code destroy:refrigerant}-tagged
 * molecule at concentration in [0.9, 1.1] M. <b>JEI display only</b> — used by
 * {@link petrolpark.mc.destroy.compat.jei.animation.HeatConditionRenderer} to show a "any
 * refrigerant" hint slot to the right of the Cooler in COOLED-condition recipe categories
 * (BasinCategory / PackingCategory / MixingCategory). {@link #testMixture} returns false so it
 * never appears as a real ingredient — recipes that actually require refrigerant use the
 * {@link MoleculeTagFluidIngredient} JSON type instead.
 *
 * <p>Codec: unit-based (no fields to serialize) — JSON {@code {"type": "destroy:refrigerant_dummy"}}
 * always deserializes to the singleton constant. Subtype id stored in the data-pack/network
 * registry via {@link DestroyFluidIngredientTypes#REFRIGERANT_DUMMY}.</p>
*/
public class RefrigerantDummyFluidIngredient extends MoleculeTagFluidIngredient {

    public static final RefrigerantDummyFluidIngredient INSTANCE = new RefrigerantDummyFluidIngredient();

    public static final MapCodec<RefrigerantDummyFluidIngredient> CODEC = MapCodec.unit(INSTANCE);

    public static final StreamCodec<RegistryFriendlyByteBuf, RefrigerantDummyFluidIngredient> STREAM_CODEC =
        StreamCodec.unit(INSTANCE);

    public RefrigerantDummyFluidIngredient() {
        // The
        // string id matches the LegacySpeciesTag singleton id in DestroyMolecules.Tags.REFRIGERANT.
        super("destroy:refrigerant", 0.9f, 1.1f);
    }

    @Override
    protected boolean testMixture(LegacyMixture mixture) {
        return false;  // JEI-display only — never an actual recipe match.
    }

    @Override
    public FluidIngredientType<?> getType() {
        return DestroyFluidIngredientTypes.REFRIGERANT_DUMMY.get();
    }

    /**
 * Override the parent's molecule_tag spec tag with our "refrigerants" marker — the tooltip
 * mixin uses this to pick the {@code tooltip.mixture_ingredient.refrigerants} lang key.
*/
    @Override
    protected net.minecraft.nbt.CompoundTag ingredientInfoTag() {
        net.minecraft.nbt.CompoundTag t = new net.minecraft.nbt.CompoundTag();
        t.putString("Subtype", "refrigerants");
        // Carry the tag id + concentration range so the tooltip can still display them as part of
        // the molecule-tag-style preamble ("Must contain molecule with tag: Refrigerant").
        t.putString("Id", "destroy:refrigerant");
        t.putFloat("MinConcentration", 0.9f);
        t.putFloat("MaxConcentration", 1.1f);
        return t;
    }
}
