package petrolpark.mc.destroy.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyTags;

public class DestroyBlockTagsProvider extends BlockTagsProvider {

    public DestroyBlockTagsProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Destroy.MOD_ID, existingFileHelper);
    };

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DestroyTags.Blocks.ACID_RAIN_DESTROYS.tag)
            .add(Blocks.GLOW_LICHEN, Blocks.HANGING_ROOTS, Blocks.LARGE_FERN, Blocks.SHORT_GRASS, Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.VINE,
                Blocks.BAMBOO_SAPLING, Blocks.LILY_PAD, Blocks.SUGAR_CANE, Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET
            ).addTag(BlockTags.CROPS)
            .addTag(BlockTags.CAVE_VINES)
            .addTag(BlockTags.FLOWERS)
            .addTag(BlockTags.LEAVES);
        
        // TODO add leaf litter, leaf carpet, bush, firefly bush, pale moss. probably set some stuff to dry grass

        tag(DestroyTags.Blocks.ACID_RAIN_SETS_DIRT.tag)
            .add(Blocks.GRASS_BLOCK, Blocks.MYCELIUM, Blocks.PODZOL, Blocks.ROOTED_DIRT);

        tag(DestroyTags.Blocks.ACID_RAIN_SETS_DEAD_BUSH.tag)
            .add(Blocks.FERN, Blocks.AZALEA, Blocks.FLOWERING_AZALEA, Blocks.SWEET_BERRY_BUSH)
            .addTag(BlockTags.SAPLINGS);
    };

    
    
};
