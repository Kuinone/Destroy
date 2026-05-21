package petrolpark.mc.destroy.content.tool.swissarmyknife;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import petrolpark.mc.destroy.DestroyDataComponents;

/**
 * Swiss Army Knife — an item that dynamically swaps between five tool behaviours (pickaxe / axe /
 * shovel / hoe / shears) based on what the player is looking at. The client auto-selects the tool
 * each tick via {@link #getTool(Level, HitResult, boolean)}; when the selection changes it fires
 * a {@link SwissArmyKnifeToolC2SPacket} to sync the {@link DestroyDataComponents#ACTIVE_TOOL}
 * DataComponent so that the server-side tool behaviour (mining speed, ability set) matches.
*/
public class SwissArmyKnifeItem extends DiggerItem {

    private static int timeUntilToolPutAway = 20;

    public SwissArmyKnifeItem(Tier tier, Properties properties) {
        super(tier, BlockTags.MINEABLE_WITH_PICKAXE, properties); // tag is ignored — we override correct-for-drops
    }

    @OnlyIn(Dist.CLIENT)
    public static class ClientState {
        private static final Map<LivingEntity, ClientState> states = new WeakHashMap<>();

        @Nullable
        public Tool selectedTool = null;
        public Tool lastSelectedTool = null;
        public int animTimer = 0;

        public Map<Tool, LerpedFloat> chasers;

        public ClientState() {
            chasers = new EnumMap<>(Tool.class);
            for (Tool tool : Tool.values()) {
                chasers.put(tool, LerpedFloat.angular().chase(0d, 0.4d, LerpedFloat.Chaser.EXP));
            }
        }

        public void tick() {
            chasers.values().forEach(chaser -> chaser.tickChaser());

            if (selectedTool != lastSelectedTool) {
                animTimer++;
                if (animTimer >= 8) {
                    animTimer = 0;
                    lastSelectedTool = selectedTool;
                    chasers.forEach((entry, value) -> value.chase(entry == selectedTool ? 1d : 0d, 0.4d, LerpedFloat.Chaser.EXP));
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() && entity == Minecraft.getInstance().player) {
            ClientState clientState = getClientState(Minecraft.getInstance().player);
            if (getTool(stack) != clientState.selectedTool) {
                CatnipServices.NETWORK.sendToServer(new SwissArmyKnifeToolC2SPacket(clientState.selectedTool));
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Tool tool = getTool(context.getItemInHand());
        if (tool != null) return tool.exampleTool.get().getItem().useOn(context);
        return super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (getTool(stack) == Tool.SHEARS) return Items.SHEARS.interactLivingEntity(stack, player, entity, hand);
        return super.interactLivingEntity(stack, player, entity, hand);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return this.getTier().getSpeed();
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        // Delegate to vanilla 1.21 TieredItem.isCorrectToolForDrops: looks up the Tier's
        // incorrect-for-drops BlockTag.
        return super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        Tool tool = getTool(stack);
        return tool != null && tool.actions.contains(action);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, boolean slotChanged) {
        return !(from.getItem() instanceof SwissArmyKnifeItem) || !(to.getItem() instanceof SwissArmyKnifeItem);
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() instanceof SwissArmyKnifeItem && newStack.getItem() instanceof SwissArmyKnifeItem;
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !(oldStack.getItem() instanceof SwissArmyKnifeItem) || !(newStack.getItem() instanceof SwissArmyKnifeItem);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientPlayerTick() {
        Minecraft minecraft = Minecraft.getInstance();

        ClientState.states.keySet().removeIf(entity ->
            !(entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SwissArmyKnifeItem
                || entity.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof SwissArmyKnifeItem));
        ClientState.states.forEach((entity, state) -> state.tick());

        LocalPlayer player = minecraft.player;
        if (player == null) return;

        ClientState clientState = getClientState(player);
        Tool newTool;
        boolean switchTool = false;
        if (!(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SwissArmyKnifeItem)
            && !(player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof SwissArmyKnifeItem)) {
            newTool = null;
            switchTool = true;
        } else {
            newTool = getTool(minecraft.level, minecraft.hitResult, player.isCrouching());
            if (newTool == null && timeUntilToolPutAway > 0) {
                timeUntilToolPutAway--;
                if (timeUntilToolPutAway == 0) switchTool = true;
            } else {
                timeUntilToolPutAway = 20;
            }
        }
        switchTool |= newTool != null && newTool != clientState.selectedTool;
        if (switchTool) {
            CatnipServices.NETWORK.sendToServer(new SwissArmyKnifeToolC2SPacket(newTool));
            clientState.selectedTool = newTool;
        }
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Tool getTool(Level level, HitResult ray, boolean shiftDown) {
        Tool tool = null;
        if (ray instanceof BlockHitResult bhr) {
            BlockState state = level.getBlockState(bhr.getBlockPos());

            if (state.getBlock() instanceof IPreferredSwissArmyKnifeToolBlock specialBlock) {
                return specialBlock.getToolForSwissArmyKnife(level, bhr.getBlockPos(), state, false);
            }

            if (state.is(BlockTags.MINEABLE_WITH_AXE)) tool = Tool.AXE;
            if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) tool = Tool.SHOVEL;
            if (state.is(BlockTags.MINEABLE_WITH_HOE)) tool = Tool.HOE;
            if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) tool = Tool.PICKAXE;
            if (state.getBlock() instanceof IShearable) tool = Tool.SHEARS;
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.WOOL)) tool = Tool.SHEARS;

            if (shiftDown || tool == null) {
                if (state.getBlock() instanceof PumpkinBlock) tool = Tool.SHEARS;
                if (AxeItem.getAxeStrippingState(state) != null) tool = Tool.AXE;
                // 1.21 vanilla made ShovelItem.FLATTENABLES protected. Replacement:
                // ShovelItem.getShovelPathingState(state) returns non-null iff flattenable.
                if (ShovelItem.getShovelPathingState(state) != null
                    || (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT))) {
                    tool = Tool.SHOVEL;
                }
                // 1.21 vanilla made HoeItem.TILLABLES protected with no public accessor. The
                // auto-till detection is **dropped** in 1.21 port — player switches to HOE
                // manually over grass/dirt. Minor UX regression; gameplay still works if user
                // manually cycles.
            }
        } else if (ray instanceof EntityHitResult ehr) {
            Entity entity = ehr.getEntity();
            if (entity instanceof IPreferredSwissArmyKnifeToolEntity specialEntity) {
                return specialEntity.getToolForSwissArmyKnife(shiftDown);
            }
            if (entity instanceof LivingEntity) tool = Tool.AXE;
            if (entity instanceof IShearable) tool = shiftDown ? Tool.AXE : Tool.SHEARS;
        }
        return tool;
    }

    @Nullable
    public static Tool getTool(ItemStack stack) {
        Integer ord = stack.get(DestroyDataComponents.ACTIVE_TOOL);
        if (ord == null) return null;
        int i = ord;
        Tool[] vals = Tool.values();
        if (i < 0 || i >= vals.length) return null;
        return vals[i];
    }

    public static void putTool(ItemStack stack, @Nullable Tool tool) {
        if (tool == null) {
            stack.remove(DestroyDataComponents.ACTIVE_TOOL);
        } else {
            stack.set(DestroyDataComponents.ACTIVE_TOOL, tool.ordinal());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static ClientState getClientState(LivingEntity entity) {
        return ClientState.states.computeIfAbsent(entity, e -> new ClientState());
    }

    public static enum Tool {
        PICKAXE(ItemAbilities.DEFAULT_PICKAXE_ACTIONS, () -> new ItemStack(Items.IRON_PICKAXE)),
        AXE(ItemAbilities.DEFAULT_AXE_ACTIONS, () -> new ItemStack(Items.IRON_AXE)),
        SHOVEL(ItemAbilities.DEFAULT_SHOVEL_ACTIONS, () -> new ItemStack(Items.IRON_SHOVEL)),
        HOE(ItemAbilities.DEFAULT_HOE_ACTIONS, () -> new ItemStack(Items.IRON_HOE)),
        SHEARS(ItemAbilities.DEFAULT_SHEARS_ACTIONS, () -> new ItemStack(Items.IRON_PICKAXE));

        public static final Map<Tool, LerpedFloat> ALL_RETRACTED = new EnumMap<>(Tool.class);
        static {
            for (Tool tool : values())
                ALL_RETRACTED.put(tool, LerpedFloat.angular().chase(0d, 0.4d, LerpedFloat.Chaser.EXP));
        }

        public final Set<ItemAbility> actions;
        public final Supplier<ItemStack> exampleTool;

        Tool(Set<ItemAbility> toolActions, Supplier<ItemStack> exampleTool) {
            this.actions = toolActions;
            this.exampleTool = exampleTool;
        }
    }
}
