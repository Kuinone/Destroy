package petrolpark.mc.destroy;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

/**
 * Food properties. 1.21.1 FoodProperties.Builder.effect now takes a {@code MobEffectInstance}
 * directly (not a supplier), and entries are added via {@code .effect(instance, probability)}.
*/
public class DestroyFoods {

    public static final FoodProperties

    APPLE_JUICE = new FoodProperties.Builder()
        .nutrition(5).saturationModifier(0.4f).alwaysEdible()
        .effect(new MobEffectInstance(DestroyMobEffects.FULL_BLADDER.getDelegate(), 600, 0, false, false, true), 1f)
        .build(),
    BUTTER = new FoodProperties.Builder().nutrition(6).saturationModifier(0.1f).build(),
    CREATINE = new FoodProperties.Builder()
        .nutrition(0).saturationModifier(0f)
        .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1), 1f)
        .build(),
    RAW_FRIES = new FoodProperties.Builder().nutrition(2).saturationModifier(0.6f).build(),
    FRIES = new FoodProperties.Builder().nutrition(6).saturationModifier(1.5f).build(),
    MASHED_POTATO = new FoodProperties.Builder().nutrition(5).saturationModifier(1.4f).build(),
    MILK_CARTON = new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).alwaysEdible().build(),
    CHEWING_GUM = new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).alwaysEdible().build(),
    POTATE_O = new FoodProperties.Builder().nutrition(2).saturationModifier(0.6f).build(),
    BIFURICATED_CARROT = new FoodProperties.Builder().nutrition(6).saturationModifier(1.2f).build(),
    BABY_BLUE_POWDER = new FoodProperties.Builder()
        .nutrition(0).saturationModifier(0.0f).alwaysEdible()
        .effect(new MobEffectInstance(DestroyMobEffects.BABY_BLUE_HIGH.getDelegate(), 600, 0, false, false, true), 1f)
        .build(),
    MOONSHINE = new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).alwaysEdible().build(),
    BANGERS_AND_MASH = new FoodProperties.Builder().nutrition(8).saturationModifier(1.8f).build();
}
