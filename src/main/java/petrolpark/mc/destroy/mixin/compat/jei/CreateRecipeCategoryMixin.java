package petrolpark.mc.destroy.mixin.compat.jei;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory.Info;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.createmod.catnip.lang.FontHelper.Palette;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.chemistry.legacy.ClientMixture;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpeciesTag;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.compat.jei.DestroyJEI;
import petrolpark.mc.destroy.config.DestroyAllConfigs;

/** 这些分子的组合浓度必须在 0.1M 与 0.3M 间"). The spec data is
 * attached to each example FluidStack as a {@link DestroyDataComponents#MIXTURE_INGREDIENT_INFO}
 * component by the ingredient subtype's {@code ingredientInfoTag()} — see
 * {@link petrolpark.mc.destroy.core.recipe.ingredient.fluid.MixtureFluidIngredient#generateStacks}.</li>
 * </ul>
 * </li>
 * </ol>
 *
 * <ol>
 * <li><b>Lost JEI's auto-centered fluid-name styling</b> — JEI auto-centers tooltip[0] when
 * it's a fluid display name. Replacing tooltip[0] preserves the centering; clearing and
 * re-adding from scratch loses it (the line is treated as plain body text).</li>
 * <li><b>"50 mB" amount displaced</b> — the amount line is added by JEI's
 * {@code setFluidRenderer} pipeline at a fixed position after the name. {@code clear()}
 * drops it; manual re-add at top puts it BETWEEN title and body, not BELOW body where
 * it belongs.</li>
 * </ol>
*/
@Mixin(value = CreateRecipeCategory.class, remap = false)
public abstract class CreateRecipeCategoryMixin<T extends Recipe<?>> {

    @Unique
    private static final DecimalFormat destroy$df = new DecimalFormat();
    static {
        destroy$df.setMinimumFractionDigits(3);
        destroy$df.setMaximumFractionDigits(3);
    }

    /** Separate from destroy$df (3 decimals for actual content "0.200M").
*/
    @Unique
    private static final DecimalFormat destroy$specDf = new DecimalFormat();
    static {
        destroy$specDf.setMinimumFractionDigits(1);
        destroy$specDf.setMaximumFractionDigits(1);
    }

    @Unique
    private static final Map<String, Class<? extends Recipe<?>>> destroy$CATEGORIES_AND_CLASSES = new HashMap<>();
    static {
        destroy$CATEGORIES_AND_CLASSES.put("mixing", MixingRecipe.class);
        destroy$CATEGORIES_AND_CLASSES.put("packing", CompactingRecipe.class);
        destroy$CATEGORIES_AND_CLASSES.put("spout_filling", FillingRecipe.class);
        destroy$CATEGORIES_AND_CLASSES.put("draining", EmptyingRecipe.class);
        destroy$CATEGORIES_AND_CLASSES.put("sequenced_assembly", SequencedAssemblyRecipe.class);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void destroy$inInit(Info<T> info, CallbackInfo ci) {
        String recipeTypeId = info.recipeType().getUid().getPath();
        if (destroy$CATEGORIES_AND_CLASSES.containsKey(recipeTypeId)) {
            DestroyJEI.MIXTURE_APPLICABLE_RECIPE_TYPES.put(
                info.recipeType(), destroy$CATEGORIES_AND_CLASSES.get(recipeTypeId));
        }
    }

    @Inject(
        method = "addPotionTooltip(Lmezz/jei/api/gui/ingredient/IRecipeSlotView;Ljava/util/List;)V",
        at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Optional;get()Ljava/lang/Object;"),
        remap = false,
        locals = LocalCapture.CAPTURE_FAILHARD)
    private static void destroy$inAddPotionTooltip(IRecipeSlotView view, List<Component> tooltip,
                                                   CallbackInfo ci, Optional<FluidStack> displayed) {
        FluidStack stack = displayed.get();
        if (!DestroyFluids.isMixture(stack)) return;

        boolean iupac = DestroyAllConfigs.CLIENT.chemistry.iupacNames.get();
        boolean isOutput = view.getRole() == RecipeIngredientRole.OUTPUT;

        // Determine the title (1st line). For output: actual mixture's translated name. For
        // input/spec: generic "混合物".
        Component title = DestroyLang.translate("mixture.mixture").component();
        List<Component> body = new ArrayList<>();

        if (isOutput) {
            CompoundTag mixtureTag = stack.get(DestroyDataComponents.MIXTURE);
            if (mixtureTag != null && !mixtureTag.isEmpty()) {
                ClientMixture mixture = ClientMixture.readNBT(ClientMixture::new, mixtureTag);
                title = mixture.getName();
                body.addAll(mixture.getContentsTooltip(iupac, false, false, stack.getAmount(), destroy$df));
            } else {
                body.add(DestroyLang.translate("mixture.empty").component());
            }
        } else {
            // INPUT / CATALYST — render spec from MIXTURE_INGREDIENT_INFO component.
            CompoundTag spec = stack.get(DestroyDataComponents.MIXTURE_INGREDIENT_INFO);
            if (spec != null && !spec.isEmpty()) {
                body.addAll(destroy$buildSpecLines(spec));
            } else {
                // No spec attached — fall back to the actual mixture content.
                CompoundTag mixtureTag = stack.get(DestroyDataComponents.MIXTURE);
                if (mixtureTag != null && !mixtureTag.isEmpty()) {
                    ClientMixture mixture = ClientMixture.readNBT(ClientMixture::new, mixtureTag);
                    body.addAll(mixture.getContentsTooltip(iupac, false, false, stack.getAmount(), destroy$df));
                }
            }
        }

        // 尝试
        // clear+rebuild + 显式 preserve amount/modFooter 引入了三连回归:
        // 1. 50 mB 显示两遍 — JEI 19 的 setFluidRenderer pipeline 在 callback 完成后会
        // *再次*追加 amount 行;我们捕获的 amount + JEI 自动追加的 amount = 两份
        // 2. modFooter 颜色/斜体丢失 — "最后一行带颜色 = modFooter" 启发式不可靠
        // (amount 行在某些 locale 下也可能带颜色)
        // 3. 标题没居中 — 这其实是误判,vanilla MC tooltip *不会* auto-center 任何行,
        // 之前 "看着居中" 只是 tooltip 框被正文撑宽、标题短显得居中
        // + modFooter,我们只换标题、插正文。
        // insert body at index 1, and let JEI's normal pipeline keep the trailing amount + mod
        // footer lines.
        // ModIdHelperMixin 强制 BLUE+ITALIC。后来用户发现这个白色 mod 名其实是 **Jade 模组**
        // 的设计行为(Jade hover overlay 把流体的 mod 名显示成白色与 vanilla item tooltip 区分
        // 开),不是 Destroy 的 bug 已删除 ModIdHelperMixin,本注释保留作历史索引;
        // 此处只做标题+正文替换,样式完全交给 JEI / Jade 各自的 pipeline。
        if (tooltip.isEmpty()) {
            tooltip.add(0, title);
        } else {
            tooltip.set(0, title);
        }
        tooltip.addAll(1, body);
    }

    
    @Unique
    private static List<Component> destroy$buildSpecLines(CompoundTag spec) {
        String subtype = spec.getString("Subtype");
        float min = spec.getFloat("MinConcentration");
        float max = spec.getFloat("MaxConcentration");
        Palette palette = Palette.GRAY_AND_WHITE;
        List<Component> lines = new ArrayList<>();

        switch (subtype) {
            case "molecule": {
                LegacySpecies sp = LegacySpecies.getMolecule(spec.getString("Id"));
                Component molName = sp == null
                    ? DestroyLang.translate("tooltip.unknown_molecule").component()
                    : sp.getName(DestroyAllConfigs.CLIENT.chemistry.iupacNames.get());
                lines.addAll(TooltipHelper.cutTextComponent(
                    DestroyLang.translate("tooltip.mixture_ingredient.molecule",
                        molName, destroy$specDf.format(min), destroy$specDf.format(max)).component(),
                    palette));
                break;
            }
            case "molecule_tag": {
                LegacySpeciesTag tag = LegacySpeciesTag.MOLECULE_TAGS.get(spec.getString("Id"));
                lines.addAll(TooltipHelper.cutStringTextComponent(
                    DestroyLang.translate("tooltip.mixture_ingredient.molecule_tag_1").string(), palette));
                if (tag != null) lines.add(tag.getFormattedName());
                lines.addAll(TooltipHelper.cutTextComponent(
                    DestroyLang.translate("tooltip.mixture_ingredient.molecule_tag_2",
                        destroy$specDf.format(min), destroy$specDf.format(max)).component(),
                    palette.primary(), palette.highlight()));
                break;
            }
            case "refrigerants": {
                // RefrigerantDummyFluidIngredient hint slot.
                // RefrigerantDummyFluidIngredient.Type#getDescription: 1) molecule_tag_1 preamble,
                // 2) tag formatted name, 3) the "refrigerants" specific tip ("use high concentration").
                LegacySpeciesTag tag = LegacySpeciesTag.MOLECULE_TAGS.get(spec.getString("Id"));
                lines.addAll(TooltipHelper.cutStringTextComponent(
                    DestroyLang.translate("tooltip.mixture_ingredient.molecule_tag_1").string(), palette));
                if (tag != null) lines.add(tag.getFormattedName());
                lines.addAll(TooltipHelper.cutStringTextComponent(
                    DestroyLang.translate("tooltip.mixture_ingredient.refrigerants").string(), palette));
                break;
            }
            case "ion": {
                LegacySpecies sp = LegacySpecies.getMolecule(spec.getString("Id"));
                Component molName = sp == null
                    ? DestroyLang.translate("tooltip.unknown_molecule").component()
                    : sp.getName(DestroyAllConfigs.CLIENT.chemistry.iupacNames.get());
                boolean anion = spec.getBoolean("Anion");
                lines.addAll(TooltipHelper.cutTextComponent(
                    DestroyLang.translate("tooltip.mixture_ingredient." + (anion ? "anion" : "cation"),
                        molName, destroy$specDf.format(min), destroy$specDf.format(max)).component(),
                    palette));
                break;
            }
            case "pure": {
                LegacySpecies sp = LegacySpecies.getMolecule(spec.getString("Id"));
                Component molName = sp == null
                    ? DestroyLang.translate("tooltip.unknown_molecule").component()
                    : sp.getName(DestroyAllConfigs.CLIENT.chemistry.iupacNames.get());
                lines.addAll(TooltipHelper.cutTextComponent(
                    DestroyLang.translate("tooltip.mixture_ingredient.pure", molName).component(),
                    palette));
                break;
            }
            default:
                lines.add(Component.literal("Unknown subtype: " + subtype).withStyle(ChatFormatting.RED));
        }
        return lines;
    }
}
