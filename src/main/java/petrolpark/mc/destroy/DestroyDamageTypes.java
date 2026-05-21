package petrolpark.mc.destroy;

import com.simibubi.create.foundation.damageTypes.DamageTypeBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class DestroyDamageTypes {

    public static class Keys {
        // `key()` MUST be inside Keys (not outer DestroyDamageTypes) to avoid a static-init
        // circular dependency: with Keys initializing via outer's `key(...)`, JVM forces outer
        // class init mid-Keys-init; outer's static fields (`ALCOHOL = new DamageTypeBuilder(Keys.ALCOHOL)
        // .build()`) then read `Keys.ALCOHOL` which is still null (Keys init not finished). In
        // Create 6.0.10 the `build()` method dereferences `this.key.location()` via `simpleMsgId()`,
        // so the null key crashes the JVM with EIIE → NPE. Create 6.0.8 had a less-eager
        // build() that didn't deref the key as immediately, masking the same circular dependency.
        public static final ResourceKey<DamageType>
            ALCOHOL = key("alcohol"),
            CHEMICAL_BURN = key("chemical_burn"),
            CHEMICAL_POISON = key("chemical_poison"),
            HEADACHE = key("headache"),
            BABY_BLUE_OVERDOSE = key("baby_blue_overdose"),
            SELF_NEEDLE = key("self_needle"),
            NEEDLE = key("needle"),
            EXTRUSION_DIE = key("extrusion_die");

        private static ResourceKey<DamageType> key(String name) {
            return ResourceKey.create(Registries.DAMAGE_TYPE, Destroy.asResource(name));
        }
    }

    public static void register() {}

    public static final DamageType

    ALCOHOL = new DamageTypeBuilder(Keys.ALCOHOL)
        .exhaustion(0.1f)
        .scaling(DamageScaling.ALWAYS)
        .build(),

    CHEMICAL_BURN = new DamageTypeBuilder(Keys.CHEMICAL_BURN)
        .exhaustion(0.1f)
        .scaling(DamageScaling.ALWAYS)
        .effects(DamageEffects.BURNING)
        .build(),

    CHEMICAL_POISON = new DamageTypeBuilder(Keys.CHEMICAL_POISON)
        .exhaustion(0.1f)
        .scaling(DamageScaling.ALWAYS)
        .effects(DamageEffects.BURNING)
        .build(),

    HEADACHE = new DamageTypeBuilder(Keys.HEADACHE)
        .exhaustion(0.2f)
        .scaling(DamageScaling.ALWAYS)
        .build(),

    BABY_BLUE_OVERDOSE = new DamageTypeBuilder(Keys.BABY_BLUE_OVERDOSE)
        .exhaustion(0.1f)
        .scaling(DamageScaling.ALWAYS)
        .build(),

    SELF_NEEDLE = new DamageTypeBuilder(Keys.SELF_NEEDLE)
        .exhaustion(0.1f)
        .scaling(DamageScaling.NEVER)
        .build(),

    NEEDLE = new DamageTypeBuilder(Keys.NEEDLE)
        .exhaustion(0.1f)
        .scaling(DamageScaling.ALWAYS)
        .build(),

    EXTRUSION_DIE = new DamageTypeBuilder(Keys.EXTRUSION_DIE)
        .exhaustion(0.1f)
        .scaling(DamageScaling.ALWAYS)
        .effects(DamageEffects.POKING)
        .build();
}
