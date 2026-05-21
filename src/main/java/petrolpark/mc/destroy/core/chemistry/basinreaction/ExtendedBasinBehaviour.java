package petrolpark.mc.destroy.core.chemistry.basinreaction;

import java.util.HashMap;
import java.util.Map;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.fluids.FluidStack;

import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.ReactionResult;
import petrolpark.mc.destroy.core.pollution.PollutionHelper;

/**
 * Behaviour attached to every {@link BasinBlockEntity} at attach-time via a
 * {@link BlockEntityBehaviourEvent} listener. Tracks two aspects of Destroy's chemistry pipeline
 * that the vanilla BasinBlockEntity doesn't know about:
 * <ol>
 * <li>Queued {@link ReactionResult Reaction Results} that should fire when the mixer above
 * finishes its stir cycle</li>
 * <li>Leftover evaporated gas-phase Mixture FluidStack — deposited back to the world pollution
 * when the behaviour is destroyed (block broken) or the basin completes another reaction</li>
 * </ol>
*/
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class ExtendedBasinBehaviour extends BlockEntityBehaviour {

    public static final BehaviourType<ExtendedBasinBehaviour> TYPE = new BehaviourType<>();

    public boolean tooFullToReact;
    private Map<ReactionResult, Integer> reactionResults;
    public FluidStack evaporatedFluid;

    public ExtendedBasinBehaviour(SmartBlockEntity be) {
        super(be);
        tooFullToReact = false;
        reactionResults = new HashMap<>();
        evaporatedFluid = FluidStack.EMPTY;
    }

    public void setReactionResults(Map<ReactionResult, Integer> results) {
        this.reactionResults = results;
    }

    @Override
    public void tick() {
        if (!blockEntity.hasLevel()) return;
        if (!(blockEntity instanceof BasinBlockEntity basin) || basin.getLevel().isClientSide()) return;

        BlockEntity potentialOperator = getWorld().getBlockEntity(getPos().above(2));
        if (potentialOperator instanceof MechanicalMixerBlockEntity mixer) {
            if (mixer.processingTicks == 1) enactReactionResults(basin);
        }
    }

    public void enactReactionResults(BasinBlockEntity basin) {
        for (ReactionResult result : reactionResults.keySet()) {
            for (int i = 0; i < reactionResults.get(result); i++) result.onBasinReaction(basin.getLevel(), basin);
        }
        reactionResults.clear();

        if (!evaporatedFluid.isEmpty()) {
            PollutionHelper.pollute(basin.getLevel(), basin.getBlockPos(), evaporatedFluid);
            evaporatedFluid = FluidStack.EMPTY;
        }
    }

    /** Block destroyed or removed. Requires block to call {@code ITE::onRemove}.*/
    public void destroy() {
        if (!evaporatedFluid.isEmpty() && blockEntity.getLevel() instanceof ServerLevel serverLevel) {
            PollutionHelper.pollute(serverLevel, blockEntity.getBlockPos(), evaporatedFluid);
            evaporatedFluid = FluidStack.EMPTY;
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        tooFullToReact = nbt.getBoolean("TooFullToReact");

        reactionResults = new HashMap<>();
        ListTag results = nbt.getList("Results", Tag.TAG_COMPOUND);
        results.forEach(tag -> {
            CompoundTag resultTag = (CompoundTag) tag;
            LegacyReaction reaction = LegacyReaction.get(resultTag.getString("Result"));
            if (reaction == null) return;
            ReactionResult result = reaction.getResult();
            if (result == null) return;
            int number = resultTag.getInt("Count");
            reactionResults.put(result, number);
        });

        // 1.21 FluidStack NBT I/O: parseOptional accepts either "no tag" (empty result) or a valid
        // codec-serialized form. Falls back to EMPTY on parse failure.
        if (nbt.contains("EvaporatedFluidStack", Tag.TAG_COMPOUND)) {
            evaporatedFluid = FluidStack.parseOptional(registries, nbt.getCompound("EvaporatedFluidStack"));
        } else {
            evaporatedFluid = FluidStack.EMPTY;
        }
    }

    @Override
    public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        nbt.putBoolean("TooFullToReact", tooFullToReact);

        nbt.put("Results", NBTHelper.writeCompoundList(
            reactionResults.entrySet().stream()
                .filter(entry -> entry.getKey().getReaction().isPresent())
                .toList(),
            entry -> {
                CompoundTag resultTag = new CompoundTag();
                resultTag.putString("Result", entry.getKey().getReaction().get().getFullId());
                resultTag.putInt("Count", entry.getValue());
                return resultTag;
            }));

        // 1.21 FluidStack NBT serialization: save(registries, CompoundTag.empty()) returns a Tag
        // encoding the stack. Empty stacks skip the field to keep NBT payload small.
        if (!evaporatedFluid.isEmpty()) {
            nbt.put("EvaporatedFluidStack", evaporatedFluid.save(registries, new CompoundTag()));
        }
    }

    @SubscribeEvent
    public static void onAttachBasinBehaviours(BlockEntityBehaviourEvent event) {
        event.forType(com.simibubi.create.AllBlockEntityTypes.BASIN.get(), basin ->
            event.attach(new ExtendedBasinBehaviour(basin)));
    }
}
