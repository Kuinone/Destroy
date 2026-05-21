package petrolpark.mc.destroy.core.data.advancement;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class SimpleAdvancementTrigger extends SimpleCriterionTrigger<SimpleAdvancementTrigger.Instance> {

    public void trigger(ServerPlayer player) {
        trigger(player, instance -> true);
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public static record Instance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player)
        ).apply(i, Instance::new));
    }
}
