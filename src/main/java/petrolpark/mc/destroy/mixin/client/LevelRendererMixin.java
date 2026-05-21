package petrolpark.mc.destroy.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.theme.Color;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;

import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.core.pollution.PollutionHelper;
import petrolpark.mc.destroy.DestroyPollutionTypes;

/**
 * Tints vanilla rain rendering by the local Level's ACID_RAIN pollution proportion.
 *
 * <pre>
 * 727: fconst_1 // r = 1.0
 * 728: fconst_1 // g = 1.0
 * 729: fconst_1 // b = 1.0
 * 730: fload 44 // a = computed alpha
 * 732: invokeinterface VertexConsumer.setColor(FFFF)
 * </pre>
 *
 * <ol>
 * <li><b>{@code @Inject} 标记 rain section 进入/退出</b>：在 {@code setShaderTexture(RAIN_LOCATION)}
 * 前置 boolean = true，在 {@code setShaderTexture(SNOW_LOCATION)} 前置 = false。</li>
 * <li><b>{@code @Redirect} 拦截 {@link VertexConsumer#setColor}</b>：只在 rain section 内替换
 * (r, g, b) 为污染计算色，alpha 不动。</li>
 * <li><b>额外保留 setShaderColor 调用</b>：兼容 shader pack 走 shader-color 路径的情况。</li>
 * </ol>
*/
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    /** Mixin {@code @Unique} 字段：渲染当前是否处于 rain 分支（true = rain，false = snow / off）.*/
    @Unique
    private boolean destroy$inRainSection = false;

    /** True when this mixin actively modified shader color in the current rain section.
 * Tracked separately from {@link #destroy$inRainSection} so we only RESET shader color when
 * we ourselves DIRTIED it — preventing 0% pollution rain from losing the implicit shader color
 * set by upstream vanilla rendering (fog / sky tint).*/
    @Unique
    private boolean destroy$shaderColorDirtied = false;

    /** Inject — vanilla 加载 rain 纹理前：标 rain section。**只在 ratio > 0 时**才碰 shader color；
 * 0% 污染时完全不动 shader 全局状态，保留 vanilla 隐式色调（雾、大气蓝灰等）。*/
    @Inject(
        method = "renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
            ordinal = 0
        )
    )
    public void destroy$beginRain(LightTexture lightTexture, float partialTick,
                                  double camX, double camY, double camZ, CallbackInfo ci) {
        destroy$inRainSection = true;
        // 只在 ratio > 0 才设 shader color。0% 时 vanilla 走 (1,1,1,alpha) per-vertex
        // 但 GLOBAL shader color 可能已被 vanilla 上游 rendering（fog / sky / lightTexture 等）
        // 设成蓝灰雾调；如果我们这里强制 reset 成 (1,1,1,1) 会把那层隐式色洗掉，rain 变纯白。
        // 隐式状态——0% 时不动是更安全的"无害默认"。
        if (destroy$rainColorAffected()) {
            float ratio = destroy$getAcidRainRatio();
            if (ratio > 0f) {
                Color color = destroy$getRainColor();
                RenderSystem.setShaderColor(color.getRedAsFloat(), color.getGreenAsFloat(),
                                            color.getBlueAsFloat(), 1f);
                destroy$shaderColorDirtied = true;
            }
        }
    }

    /** Inject — vanilla 加载 snow 纹理前：清 rain section + **仅在我们污染态 dirty 过 shader color
 * 时**重置；如果当前帧 ratio=0 没动过 shader color，则不 reset，让 vanilla 隐式状态继续生效。*/
    @Inject(
        method = "renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
            ordinal = 1
        )
    )
    public void destroy$endRainBeginSnow(LightTexture lightTexture, float partialTick,
                                         double camX, double camY, double camZ, CallbackInfo ci) {
        destroy$inRainSection = false;
        if (destroy$shaderColorDirtied) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            destroy$shaderColorDirtied = false;
        }
    }

    /** Defensive cleanup — 方法 RETURN 时清 flag；同样**只在 dirty 时** reset shader color。*/
    @Inject(
        method = "renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V",
        at = @At("RETURN")
    )
    public void destroy$onReturn(LightTexture lightTexture, float partialTick,
                                 double camX, double camY, double camZ, CallbackInfo ci) {
        destroy$inRainSection = false;
        if (destroy$shaderColorDirtied) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            destroy$shaderColorDirtied = false;
        }
    }

    /**
 * Redirect — vanilla 在 rain quad emission 里 4 个顶点都调
 * {@code vc.setColor(1f, 1f, 1f, fadeAlpha)}（bytecode 727-732 / 800-805 等）。
 *
 * <ul>
 * <li>无 Destroy 雨：白蓝半透明（vanilla {@code (1, 1, 1, fadeAlpha)} 撞 rain.png 自然色）</li>
 * <li>装 Destroy 雨：深蓝不透明（旧逻辑 0% 时硬塞 {@code 0xFF3E5EB8} = RGB(62, 94, 184)
 * → 把白色顶点染成深蓝 → 整个 rain texture 颜色乘法变暗）</li>
 * </ul>
 *
 * <p>结果：</p>
 * <ul>
 * <li>{@code ratio = 0}：返回 {@code (r, g, b, a)} = vanilla 完全一致</li>
 * <li>{@code ratio = 1}：返回 {@code (0, 1, 0, a)} = 满酸绿</li>
 * <li>中间值：线性 lerp 从 vanilla 到酸绿，alpha 保留</li>
 * </ul>
*/
    @Redirect(
        method = "renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
        )
    )
    public VertexConsumer destroy$tintRainVertex(VertexConsumer vc, float r, float g, float b, float a) {
        if (!(destroy$inRainSection && destroy$rainColorAffected())) {
            return vc.setColor(r, g, b, a);
        }
        float ratio = destroy$getAcidRainRatio();
        // 0% 时直接 pass-through vanilla 输入色（避免任何浮点偏移导致渲染差别）。
        if (ratio <= 0f) return vc.setColor(r, g, b, a);
        // Lerp from vanilla (r, g, b) toward (0, 1, 0) acid green by ratio.
        float nr = r + (0f - r) * ratio;
        float ng = g + (1f - g) * ratio;
        float nb = b + (0f - b) * ratio;
        return vc.setColor(nr, ng, nb, a);
    }

    /** 0..1 acid-rain pollution ratio with NaN/clamp guards.*/
    @Unique
    private float destroy$getAcidRainRatio() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return 0f;
        float ratio = PollutionHelper.getPollutionProportion(mc.level, DestroyPollutionTypes.ACID_RAIN.get());
        if (Float.isNaN(ratio) || ratio < 0f) return 0f;
        if (ratio > 1f) return 1f;
        return ratio;
    }

    /** Catnip {@link Color}-flavored variant — used by the {@code setShaderColor} fallback path
 * (some shader packs read the global shader color rather than per-vertex). Mirrors lerp logic
 * but anchors at vanilla white {@code 0xFFFFFFFF} since shader path doesn't have access to
 * the original (r, g, b) input args.*/
    @Unique
    private Color destroy$getRainColor() {
        float ratio = destroy$getAcidRainRatio();
        // 0xFFFFFFFF = vanilla white, 0xFF00FF00 = acid green. Anchor at white (not blue) so
        // shader-color fallback at 0% still matches vanilla.
        return new Color(Color.mixColors(0xFFFFFFFF, 0xFF00FF00, ratio));
    }

    @Unique
    private static boolean destroy$rainColorAffected() {
        // 1.21: PollutionHelper.pollutionEnabled → isPollutionEnabled (1.21 rename).
        // rainColorChanges lives on the CLIENT pollution config — visual / cosmetic gate.
        return PollutionHelper.isPollutionEnabled()
            && DestroyConfigs.client().pollution.rainColorChanges.get();
    }
}
