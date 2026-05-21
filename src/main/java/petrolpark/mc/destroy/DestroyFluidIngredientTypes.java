package petrolpark.mc.destroy;

import java.util.function.Supplier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import petrolpark.mc.destroy.core.recipe.ingredient.fluid.IonFluidIngredient;
import petrolpark.mc.destroy.core.recipe.ingredient.fluid.MoleculeFluidIngredient;
import petrolpark.mc.destroy.core.recipe.ingredient.fluid.MoleculeTagFluidIngredient;
import petrolpark.mc.destroy.core.recipe.ingredient.fluid.PureSpeciesFluidIngredient;
import petrolpark.mc.destroy.core.recipe.ingredient.fluid.RefrigerantDummyFluidIngredient;
import petrolpark.mc.destroy.core.recipe.ingredient.fluid.SaltFluidIngredient;

/**
 * Registry entry for all Destroy {@link net.neoforged.neoforge.fluids.crafting.FluidIngredient}
 * types. Each entry is keyed by a {@code destroy:*} ResourceLocation and discriminates recipe JSON
 * ingredient shapes via the {@code "type"} field.
 *
 * <p>Registry IDs:
 * <ul>
 * <li>{@code destroy:mixture_with_molecule} — {@link MoleculeFluidIngredient}</li>
 * <li>{@code destroy:mixture_with_salt} — {@link SaltFluidIngredient}</li>
 * <li>{@code destroy:mixture_with_molecule_tag} — {@link MoleculeTagFluidIngredient}</li>
 * <li>{@code destroy:mixture_with_ion} — {@link IonFluidIngredient}</li>
 * <li>{@code destroy:mixture_pure_species} — {@link PureSpeciesFluidIngredient}</li>
 * <li>{@code destroy:refrigerant_dummy} — {@link RefrigerantDummyFluidIngredient}</li>
 * </ul>
 *
 * <p>Wire: {@code DestroyFluidIngredientTypes.register(modEventBus)} in Destroy.java constructor.</p>
*/
public class DestroyFluidIngredientTypes {

    private static final DeferredRegister<FluidIngredientType<?>> FLUID_INGREDIENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES, Destroy.MOD_ID);

    public static final Supplier<FluidIngredientType<MoleculeFluidIngredient>> MIXTURE_WITH_MOLECULE =
        FLUID_INGREDIENT_TYPES.register("mixture_with_molecule",
            () -> new FluidIngredientType<>(MoleculeFluidIngredient.CODEC, MoleculeFluidIngredient.STREAM_CODEC));

    public static final Supplier<FluidIngredientType<SaltFluidIngredient>> MIXTURE_WITH_SALT =
        FLUID_INGREDIENT_TYPES.register("mixture_with_salt",
            () -> new FluidIngredientType<>(SaltFluidIngredient.CODEC, SaltFluidIngredient.STREAM_CODEC));

    public static final Supplier<FluidIngredientType<MoleculeTagFluidIngredient>> MIXTURE_WITH_MOLECULE_TAG =
        FLUID_INGREDIENT_TYPES.register("mixture_with_molecule_tag",
            () -> new FluidIngredientType<>(MoleculeTagFluidIngredient.CODEC, MoleculeTagFluidIngredient.STREAM_CODEC));

    public static final Supplier<FluidIngredientType<IonFluidIngredient>> MIXTURE_WITH_ION =
        FLUID_INGREDIENT_TYPES.register("mixture_with_ion",
            () -> new FluidIngredientType<>(IonFluidIngredient.CODEC, IonFluidIngredient.STREAM_CODEC));

    public static final Supplier<FluidIngredientType<PureSpeciesFluidIngredient>> MIXTURE_PURE_SPECIES =
        FLUID_INGREDIENT_TYPES.register("mixture_pure_species",
            () -> new FluidIngredientType<>(PureSpeciesFluidIngredient.CODEC, PureSpeciesFluidIngredient.STREAM_CODEC));

    public static final Supplier<FluidIngredientType<RefrigerantDummyFluidIngredient>> REFRIGERANT_DUMMY =
        FLUID_INGREDIENT_TYPES.register("refrigerant_dummy",
            () -> new FluidIngredientType<>(RefrigerantDummyFluidIngredient.CODEC, RefrigerantDummyFluidIngredient.STREAM_CODEC));

    public static void register(IEventBus modEventBus) {
        FLUID_INGREDIENT_TYPES.register(modEventBus);
    }
}
