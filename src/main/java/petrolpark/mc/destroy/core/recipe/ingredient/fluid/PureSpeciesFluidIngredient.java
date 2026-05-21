package petrolpark.mc.destroy.core.recipe.ingredient.fluid;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;

import petrolpark.mc.destroy.DestroyFluidIngredientTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMixture;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;

/**
 * Matches a Mixture fluid that is **pure** — contains ONLY the specified molecule (no other
 * molecules except water solvent). Used for pure-species transfer / testing recipes.
 *
 * <p>1.21 JSON: {@code {"type": "destroy:mixture_pure_species", "species": "destroy:ethanol"}}.</p>
*/
public class PureSpeciesFluidIngredient extends MixtureFluidIngredient {

    public static final MapCodec<PureSpeciesFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("species").forGetter(i -> i.speciesId)
    ).apply(instance, PureSpeciesFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PureSpeciesFluidIngredient> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, i -> i.speciesId,
            PureSpeciesFluidIngredient::new
        );

    protected final String speciesId;

    public PureSpeciesFluidIngredient(String speciesId) {
        this.speciesId = speciesId;
    }

    @Override
    protected boolean testMixture(LegacyMixture mixture) {
        LegacySpecies species = LegacySpecies.getMolecule(speciesId);
        if (species == null) return false;
        // Pure: only this molecule is present (excluding water)
        float targetConc = mixture.getConcentrationOf(species);
        if (targetConc <= 0f) return false;
        for (LegacySpecies other : mixture.getContents(true)) {
            if (other == species) continue;
            if ("destroy:water".equals(other.getFullID())) continue;
            if (mixture.getConcentrationOf(other) > 0.01f) return false;  // tolerance for numerical noise
        }
        return true;
    }

    @Override
    public List<ReadOnlyMixture> getExampleMixtures() {
        LegacySpecies species = LegacySpecies.getMolecule(speciesId);
        if (species == null) return List.of();
        return List.of(LegacyMixture.pure(species));
    }

    @Override
    public FluidIngredientType<?> getType() {
        return DestroyFluidIngredientTypes.MIXTURE_PURE_SPECIES.get();
    }

    
    @Override
    public java.util.Collection<LegacySpecies> getReferencedMolecules() {
        LegacySpecies species = LegacySpecies.getMolecule(speciesId);
        return species == null ? java.util.Collections.emptyList() : java.util.Collections.singletonList(species);
    }
}
