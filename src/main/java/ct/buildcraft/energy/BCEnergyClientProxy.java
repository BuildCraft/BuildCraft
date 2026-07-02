/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.energy;

import ct.buildcraft.core.client.render.RenderEngine_BC8;
import ct.buildcraft.energy.client.gui.GuiEngineIron_BC8;
import ct.buildcraft.energy.client.gui.GuiEngineStone_BC8;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BCEnergy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public abstract class BCEnergyClientProxy {
	
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
    	
        event.enqueueWork(
                () -> {
                	MenuScreens.register(BCEnergyGuis.MENU_STONE.get(), GuiEngineStone_BC8::new);
                	MenuScreens.register(BCEnergyGuis.MENU_IRON.get(), GuiEngineIron_BC8::new);}
            );
    }
    
    @SubscribeEvent
    public static void registryRender(EntityRenderersEvent.RegisterRenderers e) {

    	e.registerBlockEntityRenderer(BCEnergyBlocks.ENGINE_IRON_TILE_BC8.get(), RenderEngine_BC8::new);
    	e.registerBlockEntityRenderer(BCEnergyBlocks.ENGINE_STONE_TILE_BC8.get(), RenderEngine_BC8::new);
    }
    @SubscribeEvent
    public static void registrtTexture(Pre e){
    	BCEnergySprites.onTextureStitchPre(e);
//    	BCEnergy.LOGGER.info(e.getAtlas().location().getPath());
//    		e.addSprite(ENGINE_GUI);
//    		e.addSprite(BCEnergyFluids.OilFlowTexture);
//       		e.addSprite(BCEnergyFluids.OilStillTexture);
    }
}
