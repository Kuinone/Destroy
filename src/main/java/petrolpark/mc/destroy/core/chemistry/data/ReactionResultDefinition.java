package petrolpark.mc.destroy.core.chemistry.data;

import java.util.List;
import java.util.function.BiFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.createmod.catnip.math.VecHelper;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReactionResult;
import petrolpark.mc.destroy.chemistry.legacy.reactionresult.CombinedReactionResult;
import petrolpark.mc.destroy.chemistry.legacy.reactionresult.ExplosionReactionResult;
import petrolpark.mc.destroy.chemistry.legacy.reactionresult.PrecipitateReactionResult;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity;
import petrolpark.mc.destroy.core.explosion.SmartExplosion;

/**
 * Datapack-friendly view onto {@link ReactionResult}. Each subtype maps to one concrete
 * {@link ReactionResult} subclass through a dispatched codec:
 *
 * <pre>
 *   "result": {
 *     "type": "destroy:precipitate_item",
 *     "moles": 1.0,
 *     "item": "destroy:abs"
 *   }
 * </pre>
 *
 * <p>The Java-only {@code DestroyAdvancementReactionResult} and
 * {@code NovelCompoundSynthesizedReactionResult} are intentionally NOT exposed to datapacks in
 * this phase — they hold non-codec data (advancement triggers, dynamic molecule structures) that
 * needs a separate registry surface.</p>
 */
public sealed interface ReactionResultDefinition {

    String typeName();
    float moles();
    ReactionResult.Factory factory();

    Codec<ReactionResultDefinition> CODEC = Codec.STRING.dispatch("type",
        ReactionResultDefinition::typeName,
        ReactionResultDefinition::codecForType);

    static MapCodec<? extends ReactionResultDefinition> codecForType(String type) {
        return switch (type) {
            case "destroy:precipitate_item" -> PrecipitateItem.CODEC;
            case "destroy:explosion" -> Explosion.CODEC;
            case "destroy:combined" -> Combined.CODEC;
            default -> {
                Destroy.LOGGER.warn("Unknown reaction result type '{}', treating as no-op", type);
                yield NoOp.CODEC;
            }
        };
    }

    /** Drops an Item Stack of the given Item id when the reaction fires. */
    record PrecipitateItem(float moles, ResourceLocation item, int count) implements ReactionResultDefinition {
        public static final MapCodec<PrecipitateItem> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.optionalFieldOf("moles", 0f).forGetter(PrecipitateItem::moles),
            ResourceLocation.CODEC.fieldOf("item").forGetter(PrecipitateItem::item),
            Codec.INT.optionalFieldOf("count", 1).forGetter(PrecipitateItem::count)
        ).apply(i, PrecipitateItem::new));

        @Override public String typeName() { return "destroy:precipitate_item"; }

        @Override public ReactionResult.Factory factory() {
            Item resolved = BuiltInRegistries.ITEM.get(item);
            if (resolved == Items.AIR) {
                Destroy.LOGGER.warn("Reaction result item '{}' not found; skipping", item);
                return null;
            }
            int c = Math.max(1, count);
            return (m, r) -> new PrecipitateReactionResult(m, r, () -> new ItemStack(resolved, c));
        }
    }

    /** Triggers a {@link SmartExplosion} on the host block (Vat or Basin) when the reaction fires. */
    record Explosion(float moles, float radius, float irregularity) implements ReactionResultDefinition {
        public static final MapCodec<Explosion> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.optionalFieldOf("moles", 0f).forGetter(Explosion::moles),
            Codec.FLOAT.optionalFieldOf("radius", 2f).forGetter(Explosion::radius),
            Codec.FLOAT.optionalFieldOf("irregularity", 0.5f).forGetter(Explosion::irregularity)
        ).apply(i, Explosion::new));

        @Override public String typeName() { return "destroy:explosion"; }

        @Override public ReactionResult.Factory factory() {
            float r = Math.max(0.1f, radius);
            float ir = Math.max(0f, irregularity);
            BiFunction<Level, Vec3, SmartExplosion> factory =
                (level, pos) -> new SmartExplosion(level, null, null, null, pos, r, ir);
            return (m, rx) -> new ExplosionReactionResult(m, rx, factory);
        }
    }

    /** Bundles several results so a single reaction can have multiple side-effects. */
    record Combined(float moles, List<ReactionResultDefinition> results) implements ReactionResultDefinition {
        public static final MapCodec<Combined> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.optionalFieldOf("moles", 0f).forGetter(Combined::moles),
            Codec.list(CODEC_REF()).fieldOf("results").forGetter(Combined::results)
        ).apply(i, Combined::new));

        // Codec self-reference is awkward in static init; this helper indirects through the
        // sealed-interface CODEC field after it has been assigned.
        private static Codec<ReactionResultDefinition> CODEC_REF() { return ReactionResultDefinition.CODEC; }

        @Override public String typeName() { return "destroy:combined"; }

        @Override public ReactionResult.Factory factory() {
            return (m, r) -> {
                CombinedReactionResult combined = new CombinedReactionResult(m, r);
                for (ReactionResultDefinition child : results) {
                    ReactionResult.Factory cf = child.factory();
                    if (cf == null) continue;
                    combined.with(cf);
                }
                return combined;
            };
        }
    }

    /** Placeholder for unknown / unsupported result types. Does nothing at runtime. */
    record NoOp(float moles) implements ReactionResultDefinition {
        public static final MapCodec<NoOp> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.FLOAT.optionalFieldOf("moles", 0f).forGetter(NoOp::moles)
        ).apply(i, NoOp::new));

        @Override public String typeName() { return "destroy:noop"; }

        @Override public ReactionResult.Factory factory() {
            return (m, r) -> new ReactionResult(m, r) {
                @Override public void onBasinReaction(Level level, BasinBlockEntity basin) {}
                @Override public void onVatReaction(Level level, VatControllerBlockEntity vc) {}
            };
        }
    }
}
