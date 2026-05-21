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
 * Matches a Mixture fluid containing BOTH an anion AND a cation molecule (with matching
 * concentrations given the charge ratio). Used by soap-crafting / salt-dissolution recipes.
 *
 * <p>1.21 JSON: {@code {"type": "destroy:mixture_with_salt", "anion": "destroy:hydroxide",
 * "cation": "destroy:sodium_ion", "concentration": 5.0}}.</p>
*/
public class SaltFluidIngredient extends MixtureFluidIngredient {

    public static final MapCodec<SaltFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("anion").forGetter(i -> i.anionId),
        Codec.STRING.fieldOf("cation").forGetter(i -> i.cationId),
        Codec.FLOAT.fieldOf("concentration").forGetter(i -> i.concentration)
    ).apply(instance, SaltFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SaltFluidIngredient> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, i -> i.anionId,
            ByteBufCodecs.STRING_UTF8, i -> i.cationId,
            ByteBufCodecs.FLOAT, i -> i.concentration,
            SaltFluidIngredient::new
        );

    protected final String anionId;
    protected final String cationId;
    protected final float concentration;

    public SaltFluidIngredient(String anionId, String cationId, float concentration) {
        this.anionId = anionId;
        this.cationId = cationId;
        this.concentration = concentration;
    }

    @Override
    protected boolean testMixture(LegacyMixture mixture) {
        LegacySpecies anion = LegacySpecies.getMolecule(anionId);
        LegacySpecies cation = LegacySpecies.getMolecule(cationId);
        if (anion == null || cation == null) return false;
        // Balance via charge ratio
        int anionCharge = Math.abs(anion.getCharge());
        int cationCharge = Math.abs(cation.getCharge());
        if (anionCharge == 0 || cationCharge == 0) return false;
        float anionConc = mixture.getConcentrationOf(anion);
        float cationConc = mixture.getConcentrationOf(cation);
        float reqAnionConc = concentration * cationCharge;
        float reqCationConc = concentration * anionCharge;
        return anionConc >= reqAnionConc && cationConc >= reqCationConc;
    }

    @Override
    public List<ReadOnlyMixture> getExampleMixtures() {
        LegacySpecies anion = LegacySpecies.getMolecule(anionId);
        LegacySpecies cation = LegacySpecies.getMolecule(cationId);
        if (anion == null || cation == null) return List.of();
        LegacyMixture m = new LegacyMixture();
        int anionCharge = Math.max(Math.abs(anion.getCharge()), 1);
        int cationCharge = Math.max(Math.abs(cation.getCharge()), 1);
        m.addMolecule(anion, concentration * cationCharge);
        m.addMolecule(cation, concentration * anionCharge);
        return List.of(m);
    }

    @Override
    public FluidIngredientType<?> getType() {
        return DestroyFluidIngredientTypes.MIXTURE_WITH_SALT.get();
    }

    
    @Override
    public java.util.Collection<LegacySpecies> getReferencedMolecules() {
        LegacySpecies anion = LegacySpecies.getMolecule(anionId);
        LegacySpecies cation = LegacySpecies.getMolecule(cationId);
        java.util.List<LegacySpecies> list = new java.util.ArrayList<>(2);
        if (anion != null) list.add(anion);
        if (cation != null) list.add(cation);
        return list;
    }
}
