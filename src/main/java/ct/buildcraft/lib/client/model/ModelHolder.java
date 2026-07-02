/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.model;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;

/** Defines an object that will hold a model, and is automatically refreshed from the filesystem when the client reloads
 * all of its resources. */
public abstract class ModelHolder {
    public final ResourceLocation modelLocation;
    protected String failReason = "";

    public ModelHolder(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        if(this instanceof ModelHolderStatic)
        	ModelHolderRegistry.HOLDERS_VANILLABAKE.add(this);
        else
        	ModelHolderRegistry.HOLDERS_JSONBAKE.add(this);
    }

    public ModelHolder(String modelLocation) {
        this(new ResourceLocation(modelLocation));
    }
    
    protected void onModelBakePre(RegisterAdditional event) {
    	event.register(modelLocation);
    };

    protected abstract void onModelBake(BakingCompleted event);

    protected abstract void onTextureStitch(Set<ResourceLocation> toRegisterSprites);

    public abstract boolean hasBakedQuads();
}
