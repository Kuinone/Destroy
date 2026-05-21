package petrolpark.mc.destroy.config;

/** In the NeoForge port, the actual config registration
 * and loading lives in {@link DestroyConfigs}; this class just exposes the
 * {@code CLIENT}/{@code COMMON}/{@code SERVER} constants that the rest of the
 * codebase reads. Using accessor methods on the underlying singletons keeps
 * the port in step with {@link DestroyConfigs} without requiring every call
 * site to be rewritten.
*/
public final class DestroyAllConfigs {

    private DestroyAllConfigs() {}

    public static DestroyClientConfigs CLIENT() {
        return DestroyConfigs.client();
    }

    public static DestroyCommonConfigs COMMON() {
        return DestroyConfigs.common();
    }

    public static DestroyServerConfigs SERVER() {
        return DestroyConfigs.server();
    }

    // Field-style shims. These are not {@code static final} because the backing
    // singletons are assigned during mod construction; they are resolved lazily
    // via the accessor below. Callers that used {@code DestroyAllConfigs.CLIENT}
    // convenience during porting we also expose public static fields populated
    // at config-register time.
    public static DestroyClientConfigs CLIENT;
    public static DestroyCommonConfigs COMMON;
    public static DestroyServerConfigs SERVER;

    /** Called from {@link DestroyConfigs#register} after the three config instances exist.*/
    public static void link(DestroyClientConfigs client, DestroyCommonConfigs common, DestroyServerConfigs server) {
        CLIENT = client;
        COMMON = common;
        SERVER = server;
    }
}
