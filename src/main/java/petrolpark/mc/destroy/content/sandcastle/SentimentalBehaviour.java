package petrolpark.mc.destroy.content.sandcastle;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.core.chemistry.hazard.mobeffect.CryingMobEffect;

/**
 * A Behaviour for block entities which have an 'owner' and which should make that owner cry if
 * destroyed. Current consumer: sand castles (a Villager child who built one cries when a Player
 * stomps on it, and their gossip towards that Player worsens).
*/
public class SentimentalBehaviour extends BlockEntityBehaviour {

    public static final BehaviourType<SentimentalBehaviour> TYPE = new BehaviourType<>();

    public LivingEntity owner;

    public SentimentalBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        blockEntity.sendData();
    }

    /**
 * Cause the owner of this sentimental block to cry.
 * @param state the block state which was destroyed
 * @param player the player (if any) who destroyed it
*/
    public void onRemove(BlockState state, @Nullable Player player) {
        if (hasOwner() && getPos().distToCenterSqr(owner.getX(), owner.getY(), owner.getZ()) < 16) {
            owner.addEffect(new MobEffectInstance(DestroyMobEffects.CRYING.getDelegate(), 600, 0, false, false));
            // wire up the immediate-render broadcast that 's
            // CryingMobEffect.broadcastCryingStarted helper was created for but never connected.
            // Without this, vanilla MobEffect sync delivers the effect ~1 tick after addEffect, so
            // tears appear ~50ms late.
            // from MobEffect.addAttributeModifiers.
            CryingMobEffect.broadcastCryingStarted(owner);

            // For Villagers: worsen reputation + optionally award player advancement
            if (owner instanceof Villager villager && player != null) {
                villager.getGossips().add(villager.getUUID(), GossipType.MINOR_NEGATIVE, 20);
                if (villager.isBaby()) DestroyAdvancementTrigger.JUMP_ON_SAND_CASTLE.award(owner.level(), player);
            }
        }
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (!clientPacket && getWorld() instanceof ServerLevel server && tag.contains("BabyOwner")) {
            server.getEntity(tag.getUUID("BabyOwner"));
            // `owner`). Preserving faithful behaviour; the owner is hot-reloaded from server entity
            // cache when SandCastleBlock.setOwner runs post-build anyway.
        }
        super.read(tag, registries, clientPacket);
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (hasOwner() && !clientPacket) {
            tag.putUUID("BabyOwner", owner.getUUID());
        }
        super.write(tag, registries, clientPacket);
    }

    private boolean hasOwner() {
        return owner != null && owner.isAlive();
    }
}
