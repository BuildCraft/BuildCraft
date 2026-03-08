package buildcraft.datagen.energy;

import buildcraft.datagen.base.BCBaseTextureGenerator;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.energy.event.ChristmasHandler;
import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.fluid.BCFluidAttributes;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.resources.IResource;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

// From 1.12.2 AtlasSpriteFluid
public class EnergyOilTextureGenerator extends BCBaseTextureGenerator {

    public EnergyOilTextureGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, exFileHelper);
    }

    @Override
    public void run(DirectoryCache cache) throws IOException {
        ResourceLocation[][] fromSprites = new ResourceLocation[3][2];
        for (int h = 0; h < 3; h++) {
            fromSprites[h][0] = new ResourceLocation("buildcraftlib:fluids/heat_" + h + "_still");
            fromSprites[h][1] = new ResourceLocation("buildcraftlib:fluids/heat_" + h + "_flow");
        }

        Path mainOutput = generator.getOutputFolder();
        for (int index = 0; index < BCEnergyFluids.allStill.size(); index++) {
            BCFluid.Source fluid = BCEnergyFluids.allStill.get(index).get();
            ResourceLocation[] sprites = fromSprites[((BCFluidAttributes) fluid.getAttributes()).getHeat()];
            int lightColour = ((BCFluidAttributes) fluid.getAttributes()).getLightColour();
            int darkColour = ((BCFluidAttributes) fluid.getAttributes()).getDarkColour();
            int lightColour_christmas = ChristmasHandler.colours[index / 3][0];
            int darkColour_christmas = ChristmasHandler.colours[index / 3][1];
            // when datagen runs at Christmas...
            String normalStillTexture = fluid.getSource().getAttributes().getStillTexture().toString().replace("_christmas", "");
            String normalFlowTexture = fluid.getSource().getAttributes().getFlowingTexture().toString().replace("_christmas", "");
            try {
                // Normal
                recolourAndSave(mainOutput, new ResourceLocation(normalStillTexture), lightColour, darkColour, sprites[0], cache);
                recolourAndSave(mainOutput, new ResourceLocation(normalFlowTexture), lightColour, darkColour, sprites[1], cache);
                // Christmas
                recolourAndSave(mainOutput, new ResourceLocation(normalStillTexture + "_christmas"), lightColour_christmas, darkColour_christmas, sprites[0], cache);
                recolourAndSave(mainOutput, new ResourceLocation(normalFlowTexture + "_christmas"), lightColour_christmas, darkColour_christmas, sprites[1], cache);
            } catch (IOException e) {
                LOGGER.error("Couldn't save texture of {}", fluid.getRegistryName(), e);
            }
        }
    }

    private void recolourAndSave(Path mainOutput, ResourceLocation fluid, int light, int dark, ResourceLocation baseTexture, DirectoryCache cache) throws IOException {
        IResource basePngResource = exFileHelper.getResource(baseTexture, ResourcePackType.CLIENT_RESOURCES, ".png", "textures");
        IResource baseMcmetaResource = exFileHelper.getResource(baseTexture, ResourcePackType.CLIENT_RESOURCES, ".png.mcmeta", "textures");

        String outputFolderPath = "assets/" + fluid.getNamespace() + "/textures/";
        String pngPathStr = outputFolderPath + fluid.getPath() + ".png";
        Path pngOutputPath = mainOutput.resolve(pngPathStr);
        String mcmetaPathStr = outputFolderPath + fluid.getPath() + ".png.mcmeta";
        Path mcmetaOutputPath = mainOutput.resolve(mcmetaPathStr);

        // generate png
        InputStream basePngInputStream = basePngResource.getInputStream();
        BufferedImage bi = ImageIO.read(basePngInputStream);
        int width = bi.getWidth();
        int height = bi.getHeight();
        int minx = bi.getMinX();
        int miny = bi.getMinY();
        BufferedImage bo = new BufferedImage(width, height, bi.getType());
        for (int x = minx; x < width; x++) {
            for (int y = miny; y < height; y++) {
                // argb
                int pixel = bi.getRGB(x, y);
                bo.setRGB(x, y, calcColourFromARGBToARGB(pixel, light, dark));

            }
        }
        save(bo, cache, pngOutputPath);

        // copy mcmeta
        Reader reader = new BufferedReader(new InputStreamReader(baseMcmetaResource.getInputStream(), StandardCharsets.UTF_8));
        JsonObject mcmeta = JSONUtils.fromJson(GSON, reader, JsonObject.class);
        IDataProvider.save(GSON, cache, mcmeta, mcmetaOutputPath);

        // set generated or the model provider will throw exception
        // IllegalArgumentException: Texture buildcraftenergy:blocks/fluids/oil_heat_0_still does not exist in any known resource pack
        exFileHelper.trackGenerated(fluid, ResourcePackType.CLIENT_RESOURCES, ".png", "textures");
        exFileHelper.trackGenerated(fluid, ResourcePackType.CLIENT_RESOURCES, ".png.mcmeta", "textures");
    }

    public static int calcColourFromARGBToARGB(int argb, int light, int dark) {
        int r = recolourSubPixel(argb, light, dark, 16);
        int g = recolourSubPixel(argb, light, dark, 8);
        int b = recolourSubPixel(argb, light, dark, 0);
        int a = 0xFF;// recolourSubPixel(rgba, 24);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int recolourSubPixel(int rgba, int lightIn, int darkIn, int offset) {
        int data = (rgba >>> offset) & 0xFF;
        int dark = (darkIn >>> offset) & 0xFF;
        int light = (lightIn >>> offset) & 0xFF;
        return (dark * (256 - data) + light * data) / 256;
    }

    @Override
    public String getName() {
        return "BuildCraft Energy Oil Texture Generator";
    }
}
