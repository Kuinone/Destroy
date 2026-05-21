package petrolpark.mc.destroy.content.product.periodictable;

/**
 * BlockItem for {@link TankPeriodicTableBlock}. Exposes {@link #getColor} to the
 * {@link TankPeriodicTableBlockItemColor} client-side tint handler.
*/
public class TankPeriodicTableBlockItem extends PeriodicTableBlockItem {

    public TankPeriodicTableBlockItem(TankPeriodicTableBlock block, Properties properties) {
        super(block, properties);
    }

    public int getColor() {
        return ((TankPeriodicTableBlock) getBlock()).color;
    }
}
