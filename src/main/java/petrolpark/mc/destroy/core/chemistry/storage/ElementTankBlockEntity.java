package petrolpark.mc.destroy.core.chemistry.storage;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.recipe.RecipeFinder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.content.product.periodictable.ElementTankFillingRecipe;
import petrolpark.mc.destroy.core.block.entity.IHaveLabGoggleInformation;
import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour;
import petrolpark.mc.destroy.core.pollution.PollutingBehaviour;

/**
 * BE for {@link ElementTankBlock}. Holds a single 1-bucket Mixture-aware fluid tank. When the
 * stored fluid matches an {@code ELEMENT_TANK_FILLING} recipe, the block auto-converts itself into
 * the recipe's resultant block (preserving FACING if the result supports it).
*/
public class ElementTankBlockEntity extends SmartBlockEntity implements IHaveLabGoggleInformation {

    protected SmartFluidTankBehaviour tank;
    protected PollutingBehaviour pollutingBehaviour;

    protected final Object recipeCacheKey = new Object();

    public ElementTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new GeniusFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, 1000, false)
            .whenFluidUpdates(this::checkRecipe);
        behaviours.add(tank);

        pollutingBehaviour = new PollutingBehaviour(this);
        behaviours.add(pollutingBehaviour);
    }

    public void checkRecipe() {
        if (getLevel() == null) return;
        FluidStack fs = tank.getPrimaryHandler().getFluid();
        List<RecipeHolder<?>> candidates = RecipeFinder.get(recipeCacheKey, getLevel(),
            r -> r.value().getType() == DestroyRecipeTypes.ELEMENT_TANK_FILLING.getType());
        candidates.stream()
            .map(RecipeHolder::value)
            .filter(ElementTankFillingRecipe.class::isInstance)
            .map(ElementTankFillingRecipe.class::cast)
            .filter(r -> {
                var ingredient = r.getRequiredFluid();
                return ingredient.ingredient().test(fs) && ingredient.amount() <= fs.getAmount();
            })
            .findFirst()
            .ifPresent(r -> {
                BlockState state = r.blockResult.defaultBlockState();
                if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    state = state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                        getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
                }
                getLevel().setBlockAndUpdate(getBlockPos(), state);
            });
    }

    
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            DestroyBlockEntityTypes.ELEMENT_TANK.get(),
            (be, context) -> be.tank.getCapability());
    }

    public FluidStack getRenderedFluid() {
        return tank.getPrimaryTank().getRenderedFluid();
    }

    public float getFluidLevel(float partialTicks) {
        return tank.getPrimaryTank().getFluidLevel().getValue(partialTicks);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        DestroyLang.tankInfoTooltip(tooltip,
            DestroyLang.builder().add(Component.translatable("block.destroy.element_tank")),
            tank.getPrimaryHandler());
        return true;
    }
}
