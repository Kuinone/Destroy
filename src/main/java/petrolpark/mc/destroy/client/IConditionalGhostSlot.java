package petrolpark.mc.destroy.client;

/**
 * Marker interface for Create {@code ValueBoxTransform}-style ghost slots that can toggle
 * visibility / validity at runtime. Consumed by mixins and BER hooks elsewhere in Destroy.
*/
public interface IConditionalGhostSlot {
    public boolean isValid();
}
