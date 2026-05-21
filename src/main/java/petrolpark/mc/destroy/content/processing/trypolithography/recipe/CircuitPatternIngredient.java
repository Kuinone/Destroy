package petrolpark.mc.destroy.content.processing.trypolithography.recipe;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyIngredientTypes;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItem;

/**
 * Custom ingredient that matches a {@link CircuitPatternItem} stack carrying a specific named
 * {@link petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternHandler}-resolved
 * pattern. Used by recipes such as the Colorimeter / Pollutometer / Redstone Programmer where the
 * crafting check is "this MUST be a circuit board punched with the world-specific
 * {@code destroy:shared_hard_0} (etc.) pattern" — vanilla Ingredient can't express that since the
 * pattern int is generated at server-load time and isn't a static item attribute.
*/
public class CircuitPatternIngredient implements ICustomIngredient {

    public static final MapCodec<CircuitPatternIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("item").forGetter(i -> BuiltInRegistries.ITEM.getKey(i.item)),
        ResourceLocation.CODEC.fieldOf("pattern").forGetter(i -> i.patternRL)
    ).apply(instance, (itemRL, patternRL) -> new CircuitPatternIngredient(BuiltInRegistries.ITEM.get(itemRL), patternRL)));

    public static final StreamCodec<RegistryFriendlyByteBuf, CircuitPatternIngredient> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.fromCodec(ResourceLocation.CODEC), i -> BuiltInRegistries.ITEM.getKey(i.item),
            ByteBufCodecs.fromCodec(ResourceLocation.CODEC), i -> i.patternRL,
            (itemRL, patternRL) -> new CircuitPatternIngredient(BuiltInRegistries.ITEM.get(itemRL), patternRL)
        );

    protected final CircuitPatternItem item;
    protected final ResourceLocation patternRL;

    public CircuitPatternIngredient(Item item, ResourceLocation patternRL) {
        if (!(item instanceof CircuitPatternItem cpi)) {
            throw new IllegalArgumentException("Circuit pattern item ingredients must be able to have circuit patterns");
        }
        this.item = cpi;
        this.patternRL = patternRL;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.getItem().equals(item)) return false;
        Integer pattern = Destroy.CIRCUIT_PATTERN_HANDLER.getPattern(patternRL);
        if (pattern == null) return false;
        return pattern.intValue() == CircuitPatternItem.getPattern(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        Integer pattern;
        try {
            pattern = Destroy.CIRCUIT_PATTERN_HANDLER.getPattern(patternRL);
        } catch (IllegalStateException ise) {
            // Pattern not yet registered (e.g. JEI on client before server sync) — show no example.
            return Stream.empty();
        }
        if (pattern == null) return Stream.empty();
        ItemStack stack = new ItemStack(item);
        CircuitPatternItem.putPattern(stack, pattern.intValue());
        return Stream.of(stack);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return DestroyIngredientTypes.CIRCUIT_PATTERN_ITEM.get();
    }
}
