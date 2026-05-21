package petrolpark.mc.destroy.core.chemistry.vat.observation;

import net.createmod.catnip.platform.CatnipServices;

import petrolpark.mc.destroy.client.DestroyGuiTextures;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.core.chemistry.vat.VatSideBlockEntity;

/**
 * Concrete {@link AbstractQuantityObservingScreen} for a {@link VatSideBlockEntity}'s
 * {@code redstoneMonitor} behaviour. onClose fires a {@link
 * RedstoneQuantityMonitorThresholdChangeC2SPacket} to the server with the new thresholds.
*/
public class RedstoneMonitorVatSideScreen extends AbstractQuantityObservingScreen {

    private final VatSideBlockEntity vatSide;

    public RedstoneMonitorVatSideScreen(VatSideBlockEntity vatSide) {
        super(vatSide.redstoneMonitor,
            DestroyLang.translate("tooltip.vat.menu.quantity_observed.title").component(),
            DestroyGuiTextures.VAT_QUANTITY_OBSERVER);
        this.vatSide = vatSide;
    }

    @Override
    protected int getEditBoxY() {
        return 35;
    }

    @Override
    protected void updateThresholds(float lower, float upper) {
        CatnipServices.NETWORK.sendToServer(
            new RedstoneQuantityMonitorThresholdChangeC2SPacket(lower, upper, vatSide.getBlockPos()));
    }
}
