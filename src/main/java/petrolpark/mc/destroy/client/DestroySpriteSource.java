package petrolpark.mc.destroy.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.createmod.catnip.lang.Lang;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.GsonHelper;

import petrolpark.mc.destroy.Destroy;

/**
 * Custom
 * {@link SpriteSource} that contributes two classes of dynamically-generated sprites to the
 * {@code minecraft:blocks} + {@code minecraft:armor_trims} texture atlases:
 *
 * <ol>
 * <li><b>Universal armor-trim palette permutations</b> — for every namespace that declares a
 * {@code textures/trims/universal_trim_materials.json} file with {@code values: { trim_id:
 * palette_rl }} entries, apply the palette mapping to every texture under
 * {@code textures/trims/items/} (for the blocks atlas) or {@code textures/trims/models/armor/}
 * (for the armor_trims atlas) to emit a full per-trim-material set of sprites without the
 * user having to ship one texture per trim combination.</li>
 * <li><b>Circuit pattern tile slicing</b> — for each texture under
 * {@code textures/item/circuit_pattern/} and its paired {@code .png.mcmeta} (specifying
 * {@code tiles_start_x / _y / tile_width / tile_height}), slice the texture into a 4×4 grid
 * of tiles, emit each as {@code <source>/<index>} for the
 * {@link petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItemRenderer
 * CircuitPatternItemRenderer} to consume when drawing 16-punch-hole overlays.</li>
 * </ol>
*/
public class DestroySpriteSource implements SpriteSource {

    /** Set of trim-material IDs that should receive universal palette treatment. Populated during
 * {@link #run} from {@code textures/trims/universal_trim_materials.json} files. Read by
 * DatagenArmorTrimModel (future port) when generating per-item armor-trim models.*/
    public static Set<String> UNIVERSAL_ARMOR_TRIMS = Set.of();

    private static final Gson GSON = new Gson();

    public static final MapCodec<DestroySpriteSource> CODEC = MapCodec.unit(() -> new DestroySpriteSource(Atlas.BLOCKS));

    private final Atlas atlas;

    /**
 * Which minecraft:* atlas we're extending. Each has its own registered ResourceLocation so the
 * atlas JSON can choose via {@code "type": "destroy:blocks"} or {@code "destroy:armor_trims"}.
*/
    public enum Atlas {
        BLOCKS,
        ARMOR_TRIMS;

        /** The SpriteSourceType registered to this atlas (constructed in {@link #registerTypes}).*/
        public SpriteSourceType type;
        /** MapCodec that yields a {@code DestroySpriteSource} pointing at this atlas.*/
        public final MapCodec<DestroySpriteSource> codec;

        Atlas() {
            this.codec = MapCodec.unit(() -> new DestroySpriteSource(this));
        }
    }

    public DestroySpriteSource(Atlas atlas) {
        this.atlas = atlas;
    }

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        Destroy.LOGGER.info("Supplying custom Destroy sprites to " + atlas.name());

        // ========== Universal armor-trim palette permutations ==========

        Supplier<int[]> paletteKeySupplier = Suppliers.memoize(() -> PalettedPermutations.loadPaletteEntryFromImage(
            resourceManager, ResourceLocation.fromNamespaceAndPath("minecraft", "trims/color_palettes/trim_palette")));

        Map<String, Supplier<IntUnaryOperator>> trimMaterials = new HashMap<>();
        for (String namespace : resourceManager.getNamespaces()) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/trims/universal_trim_materials.json");
            Optional<Resource> resource = resourceManager.getResource(location);
            if (resource.isEmpty()) continue;
            try (InputStream inputStream = resource.get().open()) {
                JsonObject jsonObject = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
                jsonObject.get("values").getAsJsonObject().entrySet().forEach(entry -> {
                    ResourceLocation textureId = ResourceLocation.parse(entry.getValue().getAsString());
                    trimMaterials.put(entry.getKey(), Suppliers.memoize(() -> createPaletteMapping(
                        paletteKeySupplier.get(),
                        PalettedPermutations.loadPaletteEntryFromImage(resourceManager, textureId))));
                });
            } catch (IOException e) {
                Destroy.LOGGER.error("Failed to read universal trim material file: ", e);
            }
        }

        UNIVERSAL_ARMOR_TRIMS = Set.of(trimMaterials.keySet().toArray(i -> new String[i]));

        Map<ResourceLocation, Resource> trimTextures = new HashMap<>();
        if (atlas == Atlas.BLOCKS) {
            trimTextures = resourceManager.listResources("textures/trims/items", rl -> true);
        } else if (atlas == Atlas.ARMOR_TRIMS) {
            trimTextures = resourceManager.listResources("textures/trims/models/armor", rl -> true);
        }

        for (Entry<ResourceLocation, Resource> entry : trimTextures.entrySet()) {
            ResourceLocation file = entry.getKey();
            ResourceLocation id = TEXTURE_ID_CONVERTER.fileToId(file);
            LazyLoadedImage lazyloadedimage = new LazyLoadedImage(file, entry.getValue(), Math.max(1, trimMaterials.size()));

            for (Entry<String, Supplier<IntUnaryOperator>> trimMaterial : trimMaterials.entrySet()) {
                ResourceLocation permutationLocation = id.withSuffix("_" + trimMaterial.getKey());
                output.add(permutationLocation, new PalettedSpriteSupplier(lazyloadedimage, trimMaterial.getValue(), permutationLocation));
            }
        }

        // ========== Circuit pattern tile slicing (BLOCKS atlas only) ==========

        if (atlas != Atlas.BLOCKS) return;

        FileToIdConverter circuitConverter = new FileToIdConverter("textures/item/circuit_pattern", ".png");
        for (Entry<ResourceLocation, Resource> entry : circuitConverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation file = entry.getKey();
            ResourceLocation id = TEXTURE_ID_CONVERTER.fileToId(file);

            int xStart = 3;
            int yStart = 3;
            int tileWidth = 2;
            int tileHeight = 2;

            ResourceLocation metaFile = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), file.getPath() + ".mcmeta");
            Optional<Resource> metaFileOpened = resourceManager.getResource(metaFile);
            if (metaFileOpened.isPresent()) {
                try (Reader reader = metaFileOpened.get().openAsReader()) {
                    JsonObject obj = GsonHelper.fromJson(GSON, reader, JsonElement.class).getAsJsonObject();
                    if (obj.has("tiles_start_x")) xStart = obj.get("tiles_start_x").getAsInt();
                    if (obj.has("tiles_start_y")) yStart = obj.get("tiles_start_y").getAsInt();
                    if (obj.has("tile_width"))    tileWidth  = obj.get("tile_width").getAsInt();
                    if (obj.has("tile_height"))   tileHeight = obj.get("tile_height").getAsInt();
                } catch (JsonSyntaxException | IOException e) {
                    Destroy.LOGGER.error("Failed to read .mcmeta file for Circuit Pattern texture: " + file, e);
                    continue;
                }
            } else {
                Destroy.LOGGER.error("No .mcmeta file for Circuit Pattern texture: " + file, new FileNotFoundException());
                continue;
            }

            // Slice into 4×4 tiles, emit each as <source>/<index>
            try (NativeImage image = new LazyLoadedImage(file, entry.getValue(), 1).get()) {
                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        NativeImage partialImage = new NativeImage(Format.RGBA, image.getWidth(), image.getHeight(), false);
                        int xPos = xStart + x * tileWidth;
                        int yPos = yStart + y * tileHeight;
                        image.copyRect(partialImage, xPos, yPos, xPos, yPos, tileWidth, tileHeight, false, false);
                        int width = image.getWidth();
                        int height = image.getHeight();
                        ResourceLocation sectionId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "/" + ((y * 4) + x));
                        output.add(sectionId, new TileSpriteSupplier(sectionId, partialImage, width, height));
                    }
                }
            } catch (IOException e) {
                Destroy.LOGGER.error("Failed to open Circuit Pattern texture: " + file, e);
            }
        }
    }

    @Override
    public SpriteSourceType type() {
        return atlas.type;
    }

    /** Called from {@code RegisterSpriteSourceTypesEvent} — creates the two registry entries.*/
    public static void registerTypes(net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent event) {
        for (Atlas a : Atlas.values()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Destroy.MOD_ID, Lang.asId(a.name()));
            a.type = new SpriteSourceType(a.codec);
            event.register(id, a.type);
        }
    }

    /** Maps every base-palette color to the mod's palette color.*/
    private static IntUnaryOperator createPaletteMapping(int[] basePalette, int[] modPalette) {
        if (basePalette.length != modPalette.length) {
            Destroy.LOGGER.warn("Palette length mismatch: base={} mod={}; falling back to identity", basePalette.length, modPalette.length);
            return IntUnaryOperator.identity();
        }
        // 1.21: NativeImage.getA(int) removed from public API; inline the bit-shift.
        it.unimi.dsi.fastutil.ints.Int2IntMap int2intmap = new it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap(basePalette.length);
        for (int i = 0; i < basePalette.length; i++) {
            int baseCol = basePalette[i];
            int alpha = (baseCol >>> 24) & 0xFF;
            if (alpha != 0) {
                int2intmap.put(baseCol, modPalette[i]);
            }
        }
        return (int argb) -> {
            int a = (argb >>> 24) & 0xFF;
            if (a == 0) return argb;
            int rgbOpaque = argb | 0xFF000000;
            int replaced = int2intmap.getOrDefault(rgbOpaque, argb);
            return (replaced & 0x00FFFFFF) | (a << 24);
        };
    }

    /** SpriteSupplier for armor-trim palette permutations.*/
    public static final class PalettedSpriteSupplier implements SpriteSource.SpriteSupplier {
        private final LazyLoadedImage baseImage;
        private final Supplier<IntUnaryOperator> palette;
        private final ResourceLocation permutationLocation;

        public PalettedSpriteSupplier(LazyLoadedImage baseImage, Supplier<IntUnaryOperator> palette, ResourceLocation permutationLocation) {
            this.baseImage = baseImage;
            this.palette = palette;
            this.permutationLocation = permutationLocation;
        }

        @Override
        public SpriteContents apply(SpriteResourceLoader loader) {
            try {
                NativeImage nativeimage = baseImage.get().mappedCopy(palette.get());
                return new SpriteContents(permutationLocation,
                    new FrameSize(nativeimage.getWidth(), nativeimage.getHeight()),
                    nativeimage, ResourceMetadata.EMPTY);
            } catch (IllegalArgumentException | IOException ioexception) {
                Destroy.LOGGER.error("Unable to apply palette to {}", permutationLocation, ioexception);
            } finally {
                baseImage.release();
            }
            return null;
        }

        @Override
        public void discard() {
            baseImage.release();
        }
    }

    /** SpriteSupplier for a single circuit-pattern tile already extracted into a NativeImage.*/
    public static final class TileSpriteSupplier implements SpriteSource.SpriteSupplier {
        private final ResourceLocation id;
        private final NativeImage image;
        private final int width;
        private final int height;

        public TileSpriteSupplier(ResourceLocation id, NativeImage image, int width, int height) {
            this.id = id;
            this.image = image;
            this.width = width;
            this.height = height;
        }

        @Override
        public SpriteContents apply(SpriteResourceLoader loader) {
            return new SpriteContents(id, new FrameSize(width, height), image, ResourceMetadata.EMPTY);
        }

        @Override
        public void discard() {
            image.close();
        }
    }

    
    @SuppressWarnings("unused")
    private static <T> MetadataSectionSerializer<T> dummy() { return null; }
}
