package buildcraft.datagen.lib;

import buildcraft.datagen.base.BCBaseTextureGenerator;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class LibFrozenFluidTextureProvider extends BCBaseTextureGenerator {

    public LibFrozenFluidTextureProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, exFileHelper);
    }

    private List<ResourceLocation> createdFluids = new LinkedList<>();

    @Override
    public void run(HashCache cache) throws IOException {
        Path mainOutput = generator.getOutputFolder();
        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
            if (fluid instanceof EmptyFluid) {
                continue;
            }
            ResourceLocation still = fluid.getAttributes().getStillTexture();
            if (createdFluids.contains(still)) {
                continue;
            }
            createdFluids.add(still);
            ResourceLocation frozen = new ResourceLocation(
                    still.getNamespace(),
                    still.getPath().contains("_still")
                            ?
                            still.getPath().replace("_still", "_frozen")
                            :
                            still.getPath() + "_frozen"
            );
            genFrozen(mainOutput, still, frozen, cache);
        }

    }

    private void genFrozen(Path mainOutput, ResourceLocation still, ResourceLocation frozen, HashCache cache) throws IOException {
        Resource stillMcmetaResource = exFileHelper.getResource(still, PackType.CLIENT_RESOURCES, ".png.mcmeta", "textures");
        Resource stillResource = exFileHelper.getResource(still, PackType.CLIENT_RESOURCES, ".png", "textures");

        String folderPath = "assets/" + frozen.getNamespace() + "/textures/";
        String pngPathStr = folderPath + frozen.getPath() + ".png";
        Path pngOutputPath = mainOutput.resolve(pngPathStr);
        String mcmetaPathStr = folderPath + frozen.getPath() + ".png.mcmeta";
        Path mcmetaOutputPath = mainOutput.resolve(mcmetaPathStr);

        // generate png
        InputStream stillPngInputStream = stillResource.getInputStream();
        BufferedImage bi = ImageIO.read(stillPngInputStream);

        AnimationMetadataSection animationmetadatasection = stillResource.getMetadata(AnimationMetadataSection.SERIALIZER);
        if (animationmetadatasection == null) {
            animationmetadatasection = AnimationMetadataSection.EMPTY;
        }

        int widthOld = bi.getWidth();
        int heightOld = bi.getHeight();
        int type = bi.getType();

        int width = widthOld * 2;
        int height = heightOld * 2;
        Pair<Integer, Integer> frameSizePair = animationmetadatasection.getFrameSize(widthOld, heightOld);


        int[][] srcData = new int[frameSizePair.getFirst()][frameSizePair.getSecond()];
        for (int x = 0; x < frameSizePair.getFirst(); x++) {
            for (int y = 0; y < frameSizePair.getSecond(); y++) {
                srcData[x][y] = bi.getRGB(x, y);
            }
        }

        int mipmapLevels = 8;
        int[][] data = new int[mipmapLevels + 1][];
        for (int m = 0; m < data.length; m++) {
            data[m] = new int[width * height / (m + 1) / (m + 1)];
        }
        int[] relData = srcData[0];
        if (relData.length < (width * height / 4)) {
            Arrays.fill(data[0], 0xFF_FF_FF_00);
        } else {
            for (int x = 0; x < width; x++) {
                int fx = (x % widthOld) * heightOld;
                for (int y = 0; y < height; y++) {
                    int fy = y % heightOld;
                    data[0][x * height + y] = relData[fx + fy];
                }
            }
        }
        BufferedImage bo = new BufferedImage(width, height, type);
        for (int x = 0; x < data.length; x++) {
            int[] line = data[x];
            for (int y = 0; y < height; y++) {
                // argb
                int pixel = line[y];
                bo.setRGB(x, y, pixel);
            }
        }
        save(bo, cache, pngOutputPath);
    }

    @Override
    public String getName() {
        return "BuildCraft Frozen Fluid Texture Generator";
    }
}
