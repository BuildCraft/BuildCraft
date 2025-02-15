/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import buildcraft.lib.client.model.ModelHolderRegistry;
import buildcraft.lib.client.reload.ReloadManager;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.data.ModelVariableData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Consumer;

public enum BCLibEventDistModBus {
    INSTANCE;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
//    public void modelBake(ModelBakeEvent event)
    public void modelBake(ModelEvent.BakingCompleted event) {
//        SpriteHolderRegistry.exportTextureMap();
        SpriteHolderRegistry.exportTextureMap((TextureAtlas) Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS));
        SpriteHolderRegistry.exportTextureMap(FluidRenderer.FROZEN_ATLAS);
        LaserRenderer_BC8.clearModels();
        ModelHolderRegistry.onModelBake();
        ModelVariableData.onModelBake();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
//    public void textureStitchPre(TextureStitchEvent.Pre event)
    public void textureStitchPre(ModelEvent.ModifyBakingResult event) {
        ReloadManager.INSTANCE.preReloadResources();
//        TextureMap map = event.getMap();
//        TextureAtlas map = event.getAtlas();
//        SpriteHolderRegistry.onTextureStitchPre(map);
        SpriteHolderRegistry.onTextureStitchPre();
//        ModelHolderRegistry.onTextureStitchPre(map, event);
        ModelHolderRegistry.onTextureStitchPre();
    }

//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    @OnlyIn(Dist.CLIENT)
//    public void textureStitchPreLow(TextureStitchEvent.Pre event)
//    public void textureStitchPreLow(ModelEvent.ModifyBakingResult event) {
//        FluidRenderer.onTextureStitchPre(event.getAtlas());
//    }

    // Calen 1.20.1
    public static void onDatagenTextureRegister(Consumer<ResourceLocation> consumer, ExistingFileHelper fileHelper) {
        SpriteHolderRegistry.onDatagenTextureRegister(consumer, fileHelper);
        ModelHolderRegistry.onDatagenTextureRegister(consumer, fileHelper);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void textureStitchPost(TextureStitchEvent.Post event) {
//        // Calen: from subscriber of TextureStitchEvent.Pre
//        FluidRenderer.onTextureStitchPre();
//        ReloadManager.INSTANCE.preReloadResources();
//        TextureAtlas map = event.getAtlas();
//        SpriteHolderRegistry.onTextureStitchPre();
//        ModelHolderRegistry.onTextureStitchPre(map);

        // Calen should not map.location().equals(TextureAtlas.LOCATION_BLOCKS)
        // or the engine texture will not be loaded
        SpriteHolderRegistry.onTextureStitchPost(event);
        FluidRenderer.onTextureStitchPost(event);
    }
}
