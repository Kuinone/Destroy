package petrolpark.mc.destroy.client;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

/**
 * Push-based adapter for the 1.21 NeoForge pull-based
 * {@link QuadBakingVertexConsumer}. Delegates all {@link VertexConsumer} writes to an internal
 * {@code QuadBakingVertexConsumer} and automatically invokes
 * {@link QuadBakingVertexConsumer#bakeQuad} after every 4th vertex, emitting the resulting
 * {@link BakedQuad} to a caller-supplied {@link Consumer Consumer<BakedQuad>}.
 *
 * <h3>API contract</h3>
 * <ul>
 * <li>Caller writes vertices via {@link #addVertex(float, float, float)} + attribute setters
 * ({@link #setNormal} / {@link #setColor} / {@link #setUv} / {@link #setUv1} / {@link #setUv2}
 * / {@link #misc}) in groups of 4 per quad.</li>
 * <li>Attribute writes apply to the <b>most recent</b> {@code addVertex} — matches the 1.21
 * auto-advancing {@code addVertex} semantic where each {@code addVertex} call advances the
 * internal vertex index before subsequent attribute writes land on that slot.</li>
 * <li>The wrapper lazily detects quad boundaries: on the 5th {@code addVertex} (or
 * {@link #finish}), it calls {@code inner.bakeQuad()} first → pushes the resulting
 * {@link BakedQuad} to the sink → resets state → delegates the current {@code addVertex} call
 * to begin the next quad.</li>
 * <li><b>Caller MUST invoke {@link #finish}</b> at end-of-stream to emit the final quad (since
 * there's no 5th-vertex auto-trigger on the last quad).</li>
 * <li>Per-quad attribute setters ({@link #setTintIndex} / {@link #setDirection} /
 * {@link #setSprite} / {@link #setShade} / {@link #setHasAmbientOcclusion}) affect the quad
 * currently being written — call before the first vertex or at least before
 * {@code bakeQuad} triggers.</li>
 * </ul>
 *
 * <h3>Vertex-count tracking</h3>
 * <p>The wrapper counts {@code addVertex} invocations since the last {@code bakeQuad}. Internal
 * increments mirror 1.21 {@code QuadBakingVertexConsumer} which has a subtle index-update pattern
 * (first vertex writes at offset 0 without incrementing; subsequent {@code addVertex} calls
 * pre-increment). The external count here is a simple 0→1→2→3→4 with auto-flush on entering
 * {@code addVertex} while count==4, so the wrapper's trigger logic is independent of the inner's
 * internal index arithmetic.</p>
 *
 * <h3>Why this class instead of direct use?</h3>
 *
 * @see petrolpark.mc.destroy.core.chemistry.MoleculeRenderer first consumer of this wrapper
*/
public class PushingQuadBakingVertexConsumer implements VertexConsumer {

    private final QuadBakingVertexConsumer inner = new QuadBakingVertexConsumer();
    private final Consumer<BakedQuad> sink;

    /** Number of {@link #addVertex} invocations since the last {@link QuadBakingVertexConsumer#bakeQuad}.*/
    private int vertexCount = 0;

    public PushingQuadBakingVertexConsumer(Consumer<BakedQuad> sink) {
        this.sink = sink;
    }

    /**
 * Flush any pending complete quad (exactly 4 vertices buffered). Must be invoked at end of
 * stream to emit the final quad — otherwise the last 4 vertices stay in the internal buffer.
*/
    public void finish() {
        if (vertexCount == 4) {
            sink.accept(inner.bakeQuad());
            vertexCount = 0;
        }
    }

    // ---------- VertexConsumer interface ----------

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        if (vertexCount == 4) {
            // Emit the prior complete quad to the sink + reset before starting the next one
            sink.accept(inner.bakeQuad());
            vertexCount = 0;
        }
        inner.addVertex(x, y, z);
        vertexCount++;
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        inner.setNormal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        inner.setColor(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        inner.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        inner.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        inner.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer misc(VertexFormatElement element, int... rawData) {
        inner.misc(element, rawData);
        return this;
    }

    // ---------- Per-quad attribute pass-through ----------

    public void setTintIndex(int tintIndex) {
        inner.setTintIndex(tintIndex);
    }

    public void setDirection(Direction direction) {
        inner.setDirection(direction);
    }

    public void setSprite(TextureAtlasSprite sprite) {
        inner.setSprite(sprite);
    }

    public void setShade(boolean shade) {
        inner.setShade(shade);
    }

    public void setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        inner.setHasAmbientOcclusion(hasAmbientOcclusion);
    }
}
