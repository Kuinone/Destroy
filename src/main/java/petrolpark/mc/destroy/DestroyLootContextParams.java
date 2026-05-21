package petrolpark.mc.destroy;

import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import petrolpark.mc.destroy.core.explosion.SmartExplosion;

/**
 * Destroy 的 {@link LootContextParam} 注册表。
 *
 * <p>{@link #SMART_EXPLOSION} 由 {@link SmartExplosion#explodeBlock} 注入 loot context，{@code ObliterationCondition}
 * 从 context 读取用于决定是否走 obliteration drops 分支。</p>
*/
public class DestroyLootContextParams {

    public static final LootContextParam<SmartExplosion> SMART_EXPLOSION = create("smart_explosion");

    private static <T> LootContextParam<T> create(String id) {
        return new LootContextParam<>(Destroy.asResource(id));
    }
}
