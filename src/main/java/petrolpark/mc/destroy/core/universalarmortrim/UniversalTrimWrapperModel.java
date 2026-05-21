package petrolpark.mc.destroy.core.universalarmortrim;

import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;

import net.minecraft.client.resources.model.BakedModel;

/**
 * Marker subclass of Create's {@link CustomRenderedItemModel} — used by the
 * {@link UniversalArmorTrimItemOverrides} chain when wrapping Destroy's custom armor models so
 * Create's CustomRenderedItemModelRenderer dispatch can be subclass-keyed.
*/
public class UniversalTrimWrapperModel extends CustomRenderedItemModel {

    public UniversalTrimWrapperModel(BakedModel originalModel) {
        super(originalModel);
        originalModel.getOverrides();
    }
}
