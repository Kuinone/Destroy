package petrolpark.mc.destroy.core.universalarmortrim;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

import petrolpark.mc.destroy.Destroy;

/**
 * Wraps every armor item BakedModel in the registry whose ItemOverrides expose the {@code trim_type}
 * predicate (i.e., it's an armor item that supports trims) with a {@link BakedModelWrapper} that
 * substitutes a {@link UniversalArmorTrimItemOverrides} chain. Result: ANY armor — vanilla or
 * Destroy-custom — gets trim overlays for ANY trim material registered (even non-vanilla ones).
*/
// NeoForge 1.21 deprecated EventBusSubscriber.Bus enum; mod-bus auto-detected from event class
// (ModelEvent.ModifyBakingResult is mod-bus).
@EventBusSubscriber(modid = Destroy.MOD_ID, value = Dist.CLIENT)
public class UniversalArmorTrimModel extends BakedModelWrapper<BakedModel> {

    private final UniversalArmorTrimItemOverrides trimOverrides;

    public UniversalArmorTrimModel(BakedModel originalModel) {
        super(originalModel);
        trimOverrides = new UniversalArmorTrimItemOverrides(originalModel.getOverrides());
    }

    @Override
    public ItemOverrides getOverrides() {
        return trimOverrides;
    }

    public static final ResourceLocation TRIM_TYPE_PREDICATE_LOCATION =
        ResourceLocation.withDefaultNamespace("trim_type");

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        // Find every #inventory baked model whose ItemOverrides advertise the trim_type predicate.
        // Skip any non-SimpleBakedModel (custom renderers — don't override anything complicated).
        // 1.21 fix: Map<ModelResourceLocation, BakedModel>.
        List<Entry<ModelResourceLocation, BakedModel>> modelsToReplace = event.getModels().entrySet()
            .stream()
            .filter(entry -> entry.getKey().toString().endsWith("#inventory")
                && entry.getValue() instanceof SimpleBakedModel
                && Stream.of(entry.getValue().getOverrides().properties)
                    .anyMatch(TRIM_TYPE_PREDICATE_LOCATION::equals))
            .toList();
        for (Entry<ModelResourceLocation, BakedModel> entry : modelsToReplace) {
            event.getModels().put(entry.getKey(), new UniversalArmorTrimModel(entry.getValue()));
        }
    }
}
