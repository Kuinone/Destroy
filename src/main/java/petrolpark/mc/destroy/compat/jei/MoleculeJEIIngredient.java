package petrolpark.mc.destroy.compat.jei;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.api.ingredients.subtypes.UidContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.index.DestroyMolecules;
import petrolpark.mc.destroy.compat.jei.render.MoleculeBatchRenderer;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.chemistry.MoleculeDisplayItem;
import petrolpark.mc.destroy.core.chemistry.MoleculeDisplayItem.MoleculeTooltip;
import petrolpark.mc.destroy.core.chemistry.MoleculeRenderer;

/**
 * JEI custom ingredient type for {@link LegacySpecies} — used by ReactionCategory (S291 next)
 * to add molecule ingredients to recipe slots via
 * {@code builder.addIngredient(MoleculeJEIIngredient.TYPE, species)}.
*/
public class MoleculeJEIIngredient {

    private static final ItemStack illegalFish;
    static {
        illegalFish = new ItemStack(Items.COD);
        illegalFish.set(DataComponents.CUSTOM_NAME, Component.literal("Impossible Fish"));
    }

    public static final IIngredientType<LegacySpecies> TYPE = new IIngredientType<LegacySpecies>() {
        @Override
        public Class<? extends LegacySpecies> getIngredientClass() {
            return LegacySpecies.class;
        }
    };

    public static final IIngredientHelper<LegacySpecies> HELPER = new IIngredientHelper<LegacySpecies>() {

        @Override
        public IIngredientType<LegacySpecies> getIngredientType() {
            return TYPE;
        }

        @Override
        public String getDisplayName(LegacySpecies ingredient) {
            return ingredient.getName(false).getString() + ingredient.getName(true).getString();
        }

        @Override
        public String getUniqueId(LegacySpecies ingredient, UidContext context) {
            return ingredient.getFullID();
        }

        @Override
        public ResourceLocation getResourceLocation(LegacySpecies ingredient) {
            if (ingredient.isNovel()) return Destroy.asResource("novel_molecule");
            return ResourceLocation.parse(ingredient.getFullID());
        }

        @Override
        public String getDisplayModId(LegacySpecies ingredient) {
            // must return the mod ID (lowercase namespace), not the display name.
            // ModList lookup for that string and fell back to rendering the raw value as plain
            // text (white, non-italic), losing the standard BLUE+ITALIC mod-footer styling that
            // every other tooltip line gets via JEI's modId → ModList lookup. Returning the
            // actual mod ID lets JEI resolve it to the proper "Destroy" display name with the
            // BLUE+ITALIC ChatFormatting style applied automatically.
            if (ingredient.isNovel()) return Destroy.MOD_ID;
            return IIngredientHelper.super.getDisplayModId(ingredient);
        }

        @Override
        public LegacySpecies copyIngredient(LegacySpecies ingredient) {
            return ingredient; // Molecules are immutable · no copy needed.
        }

        @Override
        public String getErrorInfo(@Nullable LegacySpecies ingredient) {
            return ingredient == null ? "Molecule ingredient is null" : "Something is wrong with: " + ingredient.getFullID();
        }

        @Override
        public ItemStack getCheatItemStack(LegacySpecies ingredient) {
            // S309 restored: TEST_TUBE ported. Hypothetical/PROTON species still return illegalFish
            // (they can't be physically contained). All other species return a TEST_TUBE pre-filled
            // with a phase-appropriate mixture of that species.
            // The S309 port collapsed
            // both branches into {@code LegacyMixture.pure(ingredient)} which uses
            // {@link LegacySpecies#getPureConcentration()} = density / mass. For molecules with
            // density set as their LIQUID density (most molecules), this yields ~28.8 mol/L for
            // N2 — but N2 at room temp/pressure is a GAS at ~0.042 mol/L. User report:
            // "从jei作弊模式拿出来的装有对应物质的试管气体和液体物质密度（摩尔）应该有区别".
            // - boiling > 273K (liquid at RTP) → pure() with liquid density
            // - boiling ≤ 273K (gas at RTP) → AIR_MOLAR_DENSITY constant (~0.042 mol/L)
            // 1.21 port restores this branch using {@link DestroyFluids#AIR_MOLAR_DENSITY}.
            if (ingredient.isHypothetical() || ingredient == DestroyMolecules.PROTON) return illegalFish;
            petrolpark.mc.destroy.chemistry.legacy.LegacyMixture mixture;
            if (ingredient.getBoilingPoint() > 273f) {
                // Liquid at room temp/pressure — use liquid molar density.
                mixture = petrolpark.mc.destroy.chemistry.legacy.LegacyMixture.pure(ingredient);
            } else {
                // Gas at RTP — use ideal-gas molar density (= 1 atm @ 298K = AIR_MOLAR_DENSITY).
                mixture = new petrolpark.mc.destroy.chemistry.legacy.LegacyMixture();
                mixture.addMolecule(ingredient,
                    (float) petrolpark.mc.destroy.DestroyFluids.AIR_MOLAR_DENSITY);
            }
            return petrolpark.mc.destroy.core.chemistry.storage.testtube.TestTubeItem.of(
                petrolpark.mc.destroy.DestroyItems.TEST_TUBE.asStack(), mixture);
        }
    };

    public static final IIngredientRenderer<LegacySpecies> RENDERER = new IIngredientRenderer<LegacySpecies>() {

        public static final ResourceLocation FONT_LOCATION = Destroy.asResource("charge");
        public static final Style FONT = Style.EMPTY.withFont(FONT_LOCATION);
        private final MoleculeBatchRenderer.Cache batchRenderer = new MoleculeBatchRenderer.Cache();

        @Override
        public void render(GuiGraphics graphics, LegacySpecies ingredient) {
            if (DestroyAllConfigs.CLIENT.chemistry.fancyJEIRendering.get()) {
                MoleculeRenderer renderer = ingredient.getRenderer();

                MultiBufferSource.BufferSource buffer = graphics.bufferSource();
                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                renderer.renderItem(0, 0, 16, 16, poseStack, buffer);
                buffer.endBatch();

                if (ingredient.getCharge() != 0) {
                    String s = ingredient.getCharge() > 0 ? "+" : "-";
                    int col = 0xFFFFFF;

                    if (Math.abs(ingredient.getCharge()) > 1) {
                        s = Math.abs(ingredient.getCharge()) + s;
                    }

                    poseStack.pushPose();
                    poseStack.translate(0, 0, 100);
                    Font fontRenderer = Minecraft.getInstance().font;
                    FormattedCharSequence chargeText = FormattedCharSequence.forward(s, FONT);
                    graphics.drawString(fontRenderer, chargeText, 17 - fontRenderer.width(chargeText), -1, col, true);
                    poseStack.popPose();
                }
                poseStack.popPose();
            } else {
                graphics.renderItem(MoleculeDisplayItem.with(ingredient), 0, 0);
            }
        }

        @Override
        public void renderBatch(GuiGraphics graphics, List<BatchRenderElement<LegacySpecies>> elements) {
            if (DestroyAllConfigs.CLIENT.chemistry.fancyJEIRendering.get()) {
                batchRenderer.renderBatch(graphics, elements);
            } else {
                IIngredientRenderer.super.renderBatch(graphics, elements);
            }
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip, LegacySpecies ingredient, TooltipFlag tooltipFlag) {
            tooltip.add(ingredient.getName(DestroyAllConfigs.CLIENT.chemistry.iupacNames.get()));
            tooltip.add(new MoleculeTooltip(ingredient));
            tooltip.addAll(MoleculeDisplayItem.getLore(ingredient));
        }

        @Override
        @Deprecated
        public List<Component> getTooltip(LegacySpecies ingredient, TooltipFlag tooltipFlag) {
            return Collections.emptyList();
        }
    };
}
