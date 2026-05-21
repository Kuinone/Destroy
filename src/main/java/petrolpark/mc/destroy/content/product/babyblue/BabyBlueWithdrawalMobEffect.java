package petrolpark.mc.destroy.content.product.babyblue;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyDamageSources;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.config.DestroySubstancesConfigs;
import petrolpark.mc.destroy.core.mobeffect.UncurableMobEffect;

/**
 * "Baby Blue Withdrawal" mob effect — counterpart to Baby Blue High. Applies attribute penalties
 * (movement/attack speed/damage debuffs). For players, deals damage at a rate that scales with
 * their addiction attachment level (ln-based). Also prevents eating normal food (right-click
 * cancellation event) while the effect is active.
*/
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class BabyBlueWithdrawalMobEffect extends UncurableMobEffect {

    private static final ResourceLocation MOVEMENT_SPEED_ID =
        ResourceLocation.fromNamespaceAndPath(Destroy.MOD_ID, "effect.baby_blue_withdrawal.movement_speed");
    private static final ResourceLocation ATTACK_SPEED_ID =
        ResourceLocation.fromNamespaceAndPath(Destroy.MOD_ID, "effect.baby_blue_withdrawal.attack_speed");
    private static final ResourceLocation ATTACK_DAMAGE_ID =
        ResourceLocation.fromNamespaceAndPath(Destroy.MOD_ID, "effect.baby_blue_withdrawal.attack_damage");

    public BabyBlueWithdrawalMobEffect(MobEffectCategory category, int color) {
        super(category, color);
        Holder<Attribute> movement = Attributes.MOVEMENT_SPEED;
        Holder<Attribute> attackSpeed = Attributes.ATTACK_SPEED;
        Holder<Attribute> attackDamage = Attributes.ATTACK_DAMAGE;
        this.addAttributeModifier(movement, MOVEMENT_SPEED_ID, -0.15d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(attackSpeed, ATTACK_SPEED_ID, -0.45d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(attackDamage, ATTACK_DAMAGE_ID, -1.0d, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide()) {
            MobEffectInstance existing = livingEntity.getEffect(DestroyMobEffects.BABY_BLUE_WITHDRAWAL.getDelegate());
            int duration = existing == null ? 0 : existing.getDuration();

            if (livingEntity instanceof Player player) {
                PlayerBabyBlueAddictionAttachment addiction =
                    player.getData(DestroyAttachmentTypes.PLAYER_BABY_BLUE_ADDICTION);
                int level = addiction.getBabyBlueAddiction();
                int period = (int) Math.round(100.0 / Math.log(level + 1.0));
                if (period > 0 && duration % period == 0) {
                    livingEntity.hurt(DestroyDamageSources.babyBlueOverdose(livingEntity.level()), 1f);
                }
            } else if (duration % 50 == 0) {
                livingEntity.hurt(DestroyDamageSources.babyBlueOverdose(livingEntity.level()), 1f);
            }
        }
        return super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
 * Prevent eating normal food while in Withdrawal (unless the food is canAlwaysEat like golden
 * apple). Baby Blue Powder itself bypasses the block — so the player can self-medicate by
 * taking more Baby Blue.
*/
    @SubscribeEvent
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();

        if (!DestroySubstancesConfigs.babyBlueEnabled()) return;
        if (stack.getItem() == DestroyItems.BABY_BLUE_POWDER.get()) return;
        if (!player.hasEffect(DestroyMobEffects.BABY_BLUE_WITHDRAWAL.getDelegate())) return;

        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) return; // not edible — no block
        if (food.canAlwaysEat()) return; // golden apple etc.

        player.displayClientMessage(DestroyLang.translate("tooltip.eating_prevented.baby_blue").component(), true);
        event.setCanceled(true);
    }
}
