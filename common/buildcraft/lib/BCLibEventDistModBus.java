/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.model.ModelHolderRegistry;
import buildcraft.lib.client.reload.ReloadManager;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.data.ModelVariableData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public enum BCLibEventDistModBus {
    INSTANCE;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void modelBake(ModelBakeEvent event) {
//        SpriteHolderRegistry.exportTextureMap();
        SpriteHolderRegistry.exportTextureMap((TextureAtlas) Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS));
        SpriteHolderRegistry.exportTextureMap(FluidRenderer.FROZEN_ATLAS);
        LaserRenderer_BC8.clearModels();
        ModelHolderRegistry.onModelBake();
        ModelVariableData.onModelBake();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void textureStitchPre(TextureStitchEvent.Pre event) {
        if (!TextureAtlas.LOCATION_BLOCKS.equals(event.getAtlas().location())) {
            return;
        }
        ReloadManager.INSTANCE.preReloadResources();
//        TextureMap map = event.getMap();
//        SpriteHolderRegistry.onTextureStitchPre(map);
        SpriteHolderRegistry.onTextureStitchPre(event);
        ModelHolderRegistry.onTextureStitchPre(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @OnlyIn(Dist.CLIENT)
    public void textureStitchPreLow(TextureStitchEvent.Pre event) {
        if (!TextureAtlas.LOCATION_BLOCKS.equals(event.getAtlas().location())) {
            return;
        }
//        FluidRenderer.onTextureStitchPre(event.getAtlas());
        FluidRenderer.onTextureStitchPre(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void textureStitchPost(TextureStitchEvent.Post event) {
        // Calen should not map.location().equals(TextureAtlas.LOCATION_BLOCKS)
        // or the engine texture will not be loaded
        SpriteHolderRegistry.onTextureStitchPost(event);
        FluidRenderer.onTextureStitchPost(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onReloadFinish(EventBuildCraftReload.FinishLoad event) {
        // Note: when you need to add server-side listeners the client listeners need to be moved to BCLibProxy
        GuideManager.INSTANCE.onRegistryReload(event);
    }
}
