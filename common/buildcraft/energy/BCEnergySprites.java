package buildcraft.energy;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.energy.client.sprite.AtlasSpriteFluid;
import buildcraft.lib.fluid.BCFluid;

public class BCEnergySprites {
    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCEnergySprites.class);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();

        ResourceLocation[][] fromSprites = new ResourceLocation[3][2];
        for (int h = 0; h < 3; h++) {
            fromSprites[h][0] = new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_still");
            fromSprites[h][1] = new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_flow");
        }

        for (BCFluid f : BCEnergyFluids.allFluids) {
            AtlasSpriteFluid spriteStill = new AtlasSpriteFluid(f.getStill().toString(), fromSprites[f.getHeatValue()][0], f);
            AtlasSpriteFluid spriteFlow = new AtlasSpriteFluid(f.getFlowing().toString(), fromSprites[f.getHeatValue()][1], f);
            map.setTextureEntry(spriteStill);
            map.setTextureEntry(spriteFlow);
        }
    }
}
