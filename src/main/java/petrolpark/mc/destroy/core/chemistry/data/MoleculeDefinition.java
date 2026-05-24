package petrolpark.mc.destroy.core.chemistry.data;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMolecularStructure;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies.MoleculeBuilder;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpeciesTag;

/**
 * Datapack-serialisable description of a single chemical molecule. Loaded from
 * {@code data/<ns>/destroy/molecules/<id>.json}.
 *
 * <p>Each definition translates 1:1 into a {@link MoleculeBuilder} call chain at reload time —
 * the same code path the built-in {@code DestroyMolecules} registrations use. Structure is
 * supplied as a FROWNS string (e.g. {@code "destroy:linear:CCO"} for ethanol); see Destroy's
 * FROWNS docs for the full grammar.</p>
 *
 * <p>Molecule and tag IDs are looked up at apply-time. Unknown tag references cause that tag
 * to be skipped with a warning rather than failing the whole reaction; unknown structure tokens
 * fail the molecule itself and the loader logs + skips the entry.</p>
 */
public record MoleculeDefinition(
    String structure,
    Optional<Float> boilingPointCelsius,
    Optional<Float> boilingPointKelvins,
    Optional<Float> density,
    Optional<Integer> dipoleMoment,
    Optional<Float> molarHeatCapacity,
    Optional<Float> specificHeatCapacity,
    Optional<Float> latentHeat,
    Optional<Integer> color,
    Optional<String> translationKey,
    Optional<Boolean> hypothetical,
    List<String> tags
) {

    public static final Codec<MoleculeDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.fieldOf("structure").forGetter(MoleculeDefinition::structure),
        Codec.FLOAT.optionalFieldOf("boiling_point_celsius").forGetter(MoleculeDefinition::boilingPointCelsius),
        Codec.FLOAT.optionalFieldOf("boiling_point_kelvins").forGetter(MoleculeDefinition::boilingPointKelvins),
        Codec.FLOAT.optionalFieldOf("density").forGetter(MoleculeDefinition::density),
        Codec.INT.optionalFieldOf("dipole_moment").forGetter(MoleculeDefinition::dipoleMoment),
        Codec.FLOAT.optionalFieldOf("molar_heat_capacity").forGetter(MoleculeDefinition::molarHeatCapacity),
        Codec.FLOAT.optionalFieldOf("specific_heat_capacity").forGetter(MoleculeDefinition::specificHeatCapacity),
        Codec.FLOAT.optionalFieldOf("latent_heat").forGetter(MoleculeDefinition::latentHeat),
        Codec.INT.optionalFieldOf("color").forGetter(MoleculeDefinition::color),
        Codec.STRING.optionalFieldOf("translation_key").forGetter(MoleculeDefinition::translationKey),
        Codec.BOOL.optionalFieldOf("hypothetical").forGetter(MoleculeDefinition::hypothetical),
        Codec.list(Codec.STRING).optionalFieldOf("tags", List.of()).forGetter(MoleculeDefinition::tags)
    ).apply(i, MoleculeDefinition::new));

    /**
     * Apply this definition to {@link LegacySpecies#MOLECULES} under the given full id.
     * Returns true on success, false if a referenced structure / tag could not be resolved.
     */
    public boolean apply(ResourceLocation id) {
        String namespace = id.getNamespace();
        String path = id.getPath();
        if (LegacySpecies.FORBIDDEN_NAMESPACES.contains(namespace)) {
            Destroy.LOGGER.warn("Skipping molecule {}: '{}' is a forbidden namespace", id, namespace);
            return false;
        }

        LegacyMolecularStructure parsedStructure;
        try {
            parsedStructure = LegacyMolecularStructure.deserialize(structure);
        } catch (Throwable t) {
            Destroy.LOGGER.warn("Skipping molecule {}: failed to parse FROWNS structure '{}': {}",
                id, structure, t.getMessage());
            return false;
        }

        MoleculeBuilder builder = new MoleculeBuilder(namespace).id(path).structure(parsedStructure);

        // Boiling point: kelvins takes precedence if both supplied (more direct unit).
        if (boilingPointKelvins.isPresent()) builder.boilingPointInKelvins(boilingPointKelvins.get());
        else if (boilingPointCelsius.isPresent()) builder.boilingPoint(boilingPointCelsius.get());

        if (density.isPresent()) builder.density(density.get());
        if (dipoleMoment.isPresent()) builder.dipoleMoment(dipoleMoment.get());

        // Heat capacity: molar takes precedence (more chemistry-natural unit).
        if (molarHeatCapacity.isPresent()) builder.molarHeatCapacity(molarHeatCapacity.get());
        else if (specificHeatCapacity.isPresent()) builder.specificHeatCapacity(specificHeatCapacity.get());

        if (latentHeat.isPresent()) builder.latentHeat(latentHeat.get());
        if (color.isPresent()) builder.color(color.get());
        if (translationKey.isPresent()) builder.translationKey(translationKey.get());

        for (String tagId : tags) {
            LegacySpeciesTag tag = LegacySpeciesTag.MOLECULE_TAGS.get(tagId);
            if (tag == null) {
                Destroy.LOGGER.warn("Molecule {}: tag '{}' not registered, skipping that tag", id, tagId);
                continue;
            }
            builder.tag(tag);
        }
        if (hypothetical.orElse(false)) builder.hypothetical();

        try {
            LegacySpecies species = builder.build();
            species.markAsDatapack();
            return true;
        } catch (Throwable t) {
            Destroy.LOGGER.warn("Skipping molecule {}: builder failed: {}", id, t.getMessage());
            return false;
        }
    }
}
