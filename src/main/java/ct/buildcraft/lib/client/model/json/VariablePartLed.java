/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.model.json;

import com.google.gson.JsonObject;

import ct.buildcraft.lib.client.model.json.JsonVariableModel.ITextureGetter;
import ct.buildcraft.lib.expression.FunctionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class VariablePartLed extends VariablePartCuboidBase {
    private static final VariableFaceData FACE_DATA = new VariableFaceData();

    static {
//    	FACE_DATA.sprite = ModelLoader.White.INSTANCE;
        FACE_DATA.uvs.minU = 1 / 16.0f;
        FACE_DATA.uvs.minV = 2 / 16.0f;
        FACE_DATA.uvs.maxU = 1 / 16.0f;
        FACE_DATA.uvs.maxV = 2 / 16.0f;
    }

    public VariablePartLed(JsonObject obj, FunctionContext fnCtx) {
        super(obj, fnCtx);
    }

    @Override
    protected VariableFaceData getFaceData(Direction side, ITextureGetter spriteLookup) {
        FACE_DATA.uvs.minU = 1 / 16.0f;
        FACE_DATA.uvs.minV = 2 / 16.0f;
        FACE_DATA.uvs.maxU = 1 / 16.0f;
        FACE_DATA.uvs.maxV = 2 / 16.0f;
        return FACE_DATA;
    }
    
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
    	if(InventoryMenu.BLOCK_ATLAS == (event.getAtlas().location())) {
            FACE_DATA.sprite = event.getAtlas()
            		.getSprite(ForgeRegistries.BLOCKS.getKey(Blocks.QUARTZ_BLOCK));//TEMP
    	}

    }
}
