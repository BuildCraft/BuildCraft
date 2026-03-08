/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.energy.client.sprite.AtlasSpriteFluid;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.fluid.BCFluid;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

public class BCEnergySprites {
    public static void fmlPreInit() {
        // 1.18.2: following events are IModBusEvent
//        MinecraftForge.EVENT_BUS.register(BCEnergySprites.class);
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(BCEnergy.MODID).get()).getEventBus();
        modEventBus.register(BCEnergySprites.class);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
//        TextureMap map = event.getMap();
//
//        if (!BCLibConfig.useSwappableSprites) {
//            for (BCFluid f : BCEnergyFluids.allFluids) {
//                // So this doesn't work properly as we don't have the sprites.
//                // but that's ok as we said that these don't work if disabled ~anyway~
//                map.registerSprite(f.getStill());
//                map.registerSprite(f.getFlowing());
//            }
//            return;
//        }
//
//        ResourceLocation[][] fromSprites = new ResourceLocation[3][2];
//        for (int h = 0; h < 3; h++) {
//            fromSprites[h][0] = new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_still");
//            fromSprites[h][1] = new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_flow");
//        }
//
//        for (BCFluid f : BCEnergyFluids.allFluids) {
//            ResourceLocation[] sprites = fromSprites[f.getHeatValue()];
//            map.setTextureEntry(new AtlasSpriteFluid(f.getStill().toString(), sprites[0], f));
//            map.setTextureEntry(new AtlasSpriteFluid(f.getFlowing().toString(), sprites[1], f));
//        }
    }

    @SubscribeEvent
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
//        TextureMap map = event.getMap();
//
//        if (!BCLibConfig.useSwappableSprites) {
//            for (BCFluid f : BCEnergyFluids.allFluids) {
//                // So this doesn't work properly as we don't have the sprites.
//                // but that's ok as we said that these don't work if disabled ~anyway~
//                map.registerSprite(f.getStill());
//                map.registerSprite(f.getFlowing());
//            }
//            return;
//        }
//
//        ResourceLocation[][] fromSprites = new ResourceLocation[3][2];
//        for (int h = 0; h < 3; h++) {
//            fromSprites[h][0] = new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_still");
//            fromSprites[h][1] = new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_flow");
//        }
//
//        for (BCFluid f : BCEnergyFluids.allFluids) {
//            ResourceLocation[] sprites = fromSprites[f.getHeatValue()];
//            map.setTextureEntry(new AtlasSpriteFluid(f.getStill().toString(), sprites[0], f));
//            map.setTextureEntry(new AtlasSpriteFluid(f.getFlowing().toString(), sprites[1], f));
//        }
    }
}
