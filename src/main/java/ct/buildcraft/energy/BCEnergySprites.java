/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.energy;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

public class BCEnergySprites {

	public static final ResourceLocation fuel_flow =  new ResourceLocation("buildcraftenergy:blocks/fluids/fuel_flow");
	public static final ResourceLocation fuel_still = new ResourceLocation("buildcraftenergy:blocks/fluids/fuel_still");
	public static final ResourceLocation oil_flow = new ResourceLocation("buildcraftenergy:blocks/fluids/oil_flow");
	public static final ResourceLocation oil_still = new ResourceLocation("buildcraftenergy:blocks/fluids/oil_still");
	
	public static final ResourceLocation IRON_BACK_R = new ResourceLocation("buildcraftenergy:blocks/engine/iron/back");
	public static final ResourceLocation IRON_SIDE_R = new ResourceLocation("buildcraftenergy:blocks/engine/iron/side");
	public static final ResourceLocation STONE_BACK_R = new ResourceLocation("buildcraftenergy:blocks/engine/stone/back");
	public static final ResourceLocation STONE_SIDE_R = new ResourceLocation("buildcraftenergy:blocks/engine/stone/side");

	public static final ResourceLocation ENGINE_IRON_GUI = new ResourceLocation("buildcraftenergy:textures/gui/combustion_engine_gui.png");
	public static final ResourceLocation ENGINE_STONE_GUI = new ResourceLocation("buildcraftenergy:textures/gui/steam_engine_gui.png");
	
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
       
        if("textures/atlas/blocks.png".equals(event.getAtlas().location().getPath())) {
//    		
        	for (int h = 0; h < 3; h++) {
        		event.addSprite(new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_still"));
        		event.addSprite(new ResourceLocation("buildcraftenergy:blocks/fluids/heat_" + h + "_flow"));
            }
//        	event.addSprite(fuel_flow);
//            event.addSprite(fuel_still);
//            event.addSprite(oil_flow);
//            event.addSprite(oil_still);
    		event.addSprite(IRON_BACK_R);
    		event.addSprite(IRON_SIDE_R);
    		event.addSprite(STONE_BACK_R);
    		event.addSprite(STONE_SIDE_R);
    		
        }
    }
}
