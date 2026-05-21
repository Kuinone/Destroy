package petrolpark.mc.destroy.content.tool.swissarmyknife;

/**
 * Marker interface: any Entity that implements this tells the SwissArmyKnifeItem auto-selector
 * to skip its default LivingEntity/IShearable checks and use the returned Tool. Only called
 * client-side.
*/
public interface IPreferredSwissArmyKnifeToolEntity {
    /**
 * @param shiftDown whether the player is shift-crouching
*/
    SwissArmyKnifeItem.Tool getToolForSwissArmyKnife(boolean shiftDown);
}
