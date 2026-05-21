package petrolpark.mc.destroy.content.product.babyblue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Mth;

import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.config.DestroyConfigs;

/**
 * Per-Player baby blue addiction counter (0..maxAddictionLevel). Drives the Baby Blue Withdrawal
 * effect duration and damage rate.
 *
 * <ul>
 * <li>Forge {@code Capability<T>} + {@code CapabilityManager.get(CapabilityToken)} +
 * {@code ICapabilityProvider / LazyOptional} → NeoForge
 * {@link net.neoforged.neoforge.attachment.AttachmentType} registered in
 * {@link DestroyAttachmentTypes#PLAYER_BABY_BLUE_ADDICTION}</li>
 * <li>Manual {@code saveNBTData / loadNBTData} + {@code copyFrom(source)} → Codec-based
 * persistence with automatic copy-on-death via {@code AttachmentType.Builder.copyOnDeath()}</li>
 * <li>Access: {@code player.getCapability(CAPABILITY).ifPresent(attachment -> ...)} →
 * {@code player.getData(DestroyAttachmentTypes.PLAYER_BABY_BLUE_ADDICTION)} direct</li>
 * </ul>
*/
public class PlayerBabyBlueAddictionAttachment {

    /**
 * Persistent codec. The single scalar {@code babyBlueAddiction} rides as an int field. Empty
 * (field missing or 0) → default zero addiction.
*/
    public static final Codec<PlayerBabyBlueAddictionAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("baby_blue_addiction", 0)
            .forGetter(a -> a.babyBlueAddiction)
    ).apply(instance, PlayerBabyBlueAddictionAttachment::fromSaved));

    private int babyBlueAddiction;

    public PlayerBabyBlueAddictionAttachment() {
        this.babyBlueAddiction = 0;
    }

    private static PlayerBabyBlueAddictionAttachment fromSaved(int value) {
        PlayerBabyBlueAddictionAttachment a = new PlayerBabyBlueAddictionAttachment();
        a.babyBlueAddiction = value;
        return a;
    }

    public int getBabyBlueAddiction() {
        return this.babyBlueAddiction;
    }

    public void setBabyBlueAddiction(int babyBlueAddiction) {
        this.babyBlueAddiction = Mth.clamp(babyBlueAddiction, 0, getMaxBabyBlueAddiction());
    }

    public static int getMaxBabyBlueAddiction() {
        return DestroyConfigs.server().substances.babyBlueMaxAddictionLevel.get();
    }

    public void addBabyBlueAddiction(int change) {
        this.babyBlueAddiction = Mth.clamp(this.babyBlueAddiction + change, 0, getMaxBabyBlueAddiction());
    }
}
