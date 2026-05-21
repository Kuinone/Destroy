package petrolpark.mc.destroy.content.product.babyblue;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.config.DestroySubstancesConfigs;
import petrolpark.mc.destroy.core.mobeffect.UncurableMobEffect;

/**
 * "Baby Blue High" mob effect — fictional performance-enhancing drug. Boosts movement speed +50% /
 * attack speed +90% / attack damage +2, and grants mining speed bonus via the
 * {@link PlayerEvent.BreakSpeed} event. Also prevents animal breeding while active (resets love +
 * age to 0 on tick for adult Animals). On the last tick, applies Baby Blue Withdrawal scaled by
 * the player's current addiction level.
*/
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class BabyBlueHighMobEffect extends UncurableMobEffect {

    private static final ResourceLocation MOVEMENT_SPEED_ID =
        ResourceLocation.fromNamespaceAndPath(Destroy.MOD_ID, "effect.baby_blue_high.movement_speed");
    private static final ResourceLocation ATTACK_SPEED_ID =
        ResourceLocation.fromNamespaceAndPath(Destroy.MOD_ID, "effect.baby_blue_high.attack_speed");
    private static final ResourceLocation ATTACK_DAMAGE_ID =
        ResourceLocation.fromNamespaceAndPath(Destroy.MOD_ID, "effect.baby_blue_high.attack_damage");

    public BabyBlueHighMobEffect(MobEffectCategory category, int color) {
        super(category, color);
        Holder<Attribute> movement = Attributes.MOVEMENT_SPEED;
        Holder<Attribute> attackSpeed = Attributes.ATTACK_SPEED;
        Holder<Attribute> attackDamage = Attributes.ATTACK_DAMAGE;
        this.addAttributeModifier(movement, MOVEMENT_SPEED_ID, 0.3d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(attackSpeed, ATTACK_SPEED_ID, 0.9d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(attackDamage, ATTACK_DAMAGE_ID, 2.0d, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide()) {
            MobEffectInstance existing = livingEntity.getEffect(DestroyMobEffects.BABY_BLUE_HIGH.getDelegate());
            int duration = existing == null ? 0 : existing.getDuration();
            if (duration == 1) {
                // Apply the Baby Blue Withdrawal effect as the High runs out.
                if (livingEntity instanceof Player player) {
                    PlayerBabyBlueAddictionAttachment addiction =
                        player.getData(DestroyAttachmentTypes.PLAYER_BABY_BLUE_ADDICTION);
                    player.addEffect(new MobEffectInstance(DestroyMobEffects.BABY_BLUE_WITHDRAWAL.getDelegate(),
                        (10 + addiction.getBabyBlueAddiction()) * 20));
                }
            } else {
                livingEntity.removeEffect(DestroyMobEffects.BABY_BLUE_WITHDRAWAL.getDelegate());
            }

            if (livingEntity instanceof Player player) {
                Level level = player.level();
                DestroyAdvancementTrigger.TAKE_BABY_BLUE.award(level, player);
            }

            if (livingEntity instanceof Animal animal && !animal.isBaby()) {
                if (animal.getAge() > 0) animal.resetLove();
                animal.setAge(0);
            }
        }

        return super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
 * Give the player mining speed bonus / penalty if they have Baby Blue High or Withdrawal
 * respectively. Gated by server config {@code babyBlueEnabled}.
*/
    @SubscribeEvent
    public static void onPlayerBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!DestroySubstancesConfigs.babyBlueEnabled()) return;
        Player player = event.getEntity();
        if (player.hasEffect(DestroyMobEffects.BABY_BLUE_HIGH.getDelegate())) {
            MobEffectInstance high = player.getEffect(DestroyMobEffects.BABY_BLUE_HIGH.getDelegate());
            int amp = high == null ? 0 : high.getAmplifier();
            event.setNewSpeed(event.getOriginalSpeed()
                + DestroyConfigs.server().substances.babyBlueMiningSpeedBonus.getF() * (amp + 1));
        } else if (player.hasEffect(DestroyMobEffects.BABY_BLUE_WITHDRAWAL.getDelegate())) {
            MobEffectInstance wd = player.getEffect(DestroyMobEffects.BABY_BLUE_WITHDRAWAL.getDelegate());
            int amp = wd == null ? 0 : wd.getAmplifier();
            event.setNewSpeed(event.getOriginalSpeed()
                + DestroyConfigs.server().substances.babyBlueWidthdrawalSpeedBonus.getF() * (amp + 1));
            if (event.getNewSpeed() <= 0f) event.setNewSpeed(0f);
        }
    }
}
