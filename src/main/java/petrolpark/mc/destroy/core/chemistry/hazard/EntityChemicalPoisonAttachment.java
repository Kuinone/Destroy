package petrolpark.mc.destroy.core.chemistry.hazard;

import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

/**
 * Per-LivingEntity storage of the currently-active acutely-toxic Molecule. When an entity touches
 * a Mixture with an {@code ACUTELY_TOXIC}-tagged Molecule (via
 * {@code ChemistryHazardHelper.damage(...)}), that Molecule is recorded here + the
 * {@code CHEMICAL_POISON} MobEffect is applied. On MobEffect tick, this attachment tells the effect
 * which Molecule's name to show on the death screen / boss bar.
 *
 * <p>See {@link DestroyAttachmentTypes#ENTITY_CHEMICAL_POISON} for the registration.</p>
*/
public class EntityChemicalPoisonAttachment {

    /**
 * Codec using the FROWNS ID as a persistent, stable serialization anchor. Null molecule → empty
 * field. Re-reads LegacySpecies.getMolecule(id) on deserialize which will return the real
 * Molecule instance for any known ID or a re-built novel Molecule for a FROWNS code.
*/
    public static final Codec<EntityChemicalPoisonAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("molecule_id")
            .forGetter(a -> a.molecule == null ? Optional.empty() : Optional.ofNullable(a.molecule.getFullID()))
    ).apply(instance, EntityChemicalPoisonAttachment::fromSaved));

    @Nullable
    private LegacySpecies molecule;

    public EntityChemicalPoisonAttachment() {
        this.molecule = null;
    }

    private static EntityChemicalPoisonAttachment fromSaved(Optional<String> moleculeId) {
        EntityChemicalPoisonAttachment attachment = new EntityChemicalPoisonAttachment();
        moleculeId.ifPresent(id -> attachment.molecule = LegacySpecies.getMolecule(id));
        return attachment;
    }

    /**
 * Assign a toxic Molecule to the given entity. Refuses to overwrite an existing assignment
 *. Fires a
 * client-side sync packet so the player HUD picks up the Molecule name.
*/
    public static void setMolecule(Entity entity, LegacySpecies molecule) {
        if (!(entity instanceof LivingEntity)) return;
        EntityChemicalPoisonAttachment attachment = entity.getData(DestroyAttachmentTypes.ENTITY_CHEMICAL_POISON);
        if (attachment.molecule != null) return; // Don't replace existing poison
        attachment.molecule = molecule;
        if (entity instanceof ServerPlayer serverPlayer) {
            CatnipServices.NETWORK.sendToClient(serverPlayer, new ChemicalPoisonS2CPacket(molecule));
        }
    }

    /** Clear the toxic Molecule assignment (e.g. when the effect expires / player drinks milk).*/
    public static void removeMolecule(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;
        EntityChemicalPoisonAttachment attachment = entity.getData(DestroyAttachmentTypes.ENTITY_CHEMICAL_POISON);
        attachment.molecule = null;
        if (entity instanceof ServerPlayer serverPlayer) {
            CatnipServices.NETWORK.sendToClient(serverPlayer, new ChemicalPoisonS2CPacket((LegacySpecies) null));
        }
    }

    @Nullable
    public LegacySpecies getMolecule() {
        return molecule;
    }

    /** Direct setter for packet-driven client assignment (no refusal-on-conflict, no packet bounce).*/
    void setMoleculeDirect(@Nullable LegacySpecies molecule) {
        this.molecule = molecule;
    }
}
