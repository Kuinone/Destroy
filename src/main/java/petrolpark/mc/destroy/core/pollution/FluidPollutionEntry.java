package petrolpark.mc.destroy.core.pollution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/**
 * 绑定到某个 {@link PollutionType} 的 DataMap 值：声明「哪条 fluid tag 命中此污染」
 * 及「每 250mB 相对基线贡献多少」系数。
*/
public record FluidPollutionEntry(TagKey<Fluid> fluidTag, float multiplier) {

    public static final Codec<FluidPollutionEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        TagKey.codec(Registries.FLUID).fieldOf("fluid_tag").forGetter(FluidPollutionEntry::fluidTag),
        Codec.FLOAT.optionalFieldOf("multiplier", 1.0f).forGetter(FluidPollutionEntry::multiplier)
    ).apply(instance, FluidPollutionEntry::new));
}
