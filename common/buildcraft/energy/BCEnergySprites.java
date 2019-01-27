/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.fluid.BCFluid;

import buildcraft.energy.client.sprite.AtlasSpriteFluid;

public class BCEnergySprites {
    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCEnergySprites.class);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();

        if (!BCLibConfig.useSwappableSprites) {
            for (BCFluid f : BCEnergyFluids.allFluids) {
                // So this doesn't work properly as we don't have the sprites.
                // but that's ok as we said that these don't work if disabled ~anyway~
                map.registerSprite(f.getStill());
                map.registerSprite(f.getFlowing());
            }
            return;
        }

        ResourceLocation[][] fromSprites = new ResourceLocation[3][2];
        for (int h = 0; h < 3; h++) {
            fromSprites[h][0] = new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_still");
            fromSprites[h][1] = new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_flow");
        }

        for (BCFluid f : BCEnergyFluids.allFluids) {
            ResourceLocation[] sprites = fromSprites[f.getHeatValue()];
            map.setTextureEntry(new AtlasSpriteFluid(f.getStill().toString(), sprites[0], f));
            map.setTextureEntry(new AtlasSpriteFluid(f.getFlowing().toString(), sprites[1], f));
        }
    }
}
