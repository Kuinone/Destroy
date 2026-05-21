package petrolpark.mc.destroy.core.explosion;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import petrolpark.mc.destroy.DestroyLootConditions;
import petrolpark.mc.destroy.DestroyLootContextParams;

/**
 * {@link LootItemCondition} that fires when the active loot context carries a
 * {@link DestroyLootContextParams#SMART_EXPLOSION} whose
 * {@link SmartExplosion#shouldDoObliterationDrops()} returns {@code true}.
*/
public class ObliterationCondition implements LootItemCondition {

    public static final ObliterationCondition INSTANCE = new ObliterationCondition();

    public static final MapCodec<ObliterationCondition> CODEC = MapCodec.unit(() -> INSTANCE);

    private ObliterationCondition() {}

    @Override
    public boolean test(LootContext context) {
        SmartExplosion explosion = context.getParamOrNull(DestroyLootContextParams.SMART_EXPLOSION);
        return explosion != null && explosion.shouldDoObliterationDrops();
    }

    @Override
    public LootItemConditionType getType() {
        return DestroyLootConditions.OBLITERATION.get();
    }
}
