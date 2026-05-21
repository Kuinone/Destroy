package petrolpark.mc.destroy.config;

// Copied and adapted from com.simibubi.create.infrastructure.config.CStress

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.Destroy;

public class DestroyStressConfigs extends ConfigBase {

    // bump this version to reset configured values.
    private static final int VERSION = 1;

    // IDs need to be used since configs load before registration
    private static final Object2DoubleMap<ResourceLocation> DEFAULT_IMPACTS = new Object2DoubleOpenHashMap<>();
    private static final Object2DoubleMap<ResourceLocation> DEFAULT_CAPACITIES = new Object2DoubleOpenHashMap<>();

    protected final Map<ResourceLocation, ModConfigSpec.ConfigValue<Double>> capacities = new HashMap<>();
    protected final Map<ResourceLocation, ModConfigSpec.ConfigValue<Double>> impacts = new HashMap<>();

    @Override
    public void registerAll(ModConfigSpec.Builder builder) {
        builder.comment(".", Comments.su, Comments.impact)
            .push("impact");
        DEFAULT_IMPACTS.forEach((id, value) -> this.impacts.put(id, builder.define(id.getPath(), value)));
        builder.pop();

        builder.comment(".", Comments.su, Comments.capacity)
            .push("capacity");
        DEFAULT_CAPACITIES.forEach((id, value) -> this.capacities.put(id, builder.define(id.getPath(), value)));
        builder.pop();
    }

    @Override
    public String getName() {
        return "stressValues.v" + VERSION;
    }

    @Nullable
    public DoubleSupplier getImpact(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        ModConfigSpec.ConfigValue<Double> value = this.impacts.get(id);
        return value == null ? null : value::get;
    }

    @Nullable
    public DoubleSupplier getCapacity(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        ModConfigSpec.ConfigValue<Double> value = this.capacities.get(id);
        return value == null ? null : value::get;
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setNoImpact() {
        return setImpact(0);
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setImpact(double value) {
        return builder -> {
            assertFromDestroy(builder);
            ResourceLocation id = Destroy.asResource(builder.getName());
            DEFAULT_IMPACTS.put(id, value);
            return builder;
        };
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setCapacity(double value) {
        return builder -> {
            assertFromDestroy(builder);
            ResourceLocation id = Destroy.asResource(builder.getName());
            DEFAULT_CAPACITIES.put(id, value);
            return builder;
        };
    }

    private static void assertFromDestroy(BlockBuilder<?, ?> builder) {
        if (!builder.getOwner().getModid().equals(Destroy.MOD_ID)) {
            throw new IllegalStateException("Non-Destroy blocks cannot be added to Destroy's config.");
        }
    }

    private static class Comments {
        static String su = "[in Stress Units]";
        static String impact =
            "Configure the individual stress impact of mechanical blocks. Note that this cost is doubled for every speed increase it receives.";
        static String capacity = "Configure how much stress a source can accommodate for.";
    }
}
