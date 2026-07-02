/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport;

import ct.buildcraft.api.transport.pipe.PipeApiClient;
import ct.buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import ct.buildcraft.lib.client.model.ModelHolderStatic;
import ct.buildcraft.lib.client.model.ModelHolderVariable;
import ct.buildcraft.lib.client.model.ModelPluggableItem;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.client.model.plug.PlugBakerSimple;
import ct.buildcraft.lib.expression.DefaultContexts;
import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.expression.node.value.NodeVariableObject;
import ct.buildcraft.transport.client.PipeBlockColours;
import ct.buildcraft.transport.client.model.ModelPipe;
import ct.buildcraft.transport.client.model.ModelPipeItem;
import ct.buildcraft.transport.client.model.key.KeyPlugBlocker;
import ct.buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;
import ct.buildcraft.transport.client.render.PipeBehaviourRendererStripes;
import ct.buildcraft.transport.client.render.PipeFlowRendererFluids;
import ct.buildcraft.transport.client.render.PipeFlowRendererItems;
import ct.buildcraft.transport.client.render.PipeFlowRendererPower;
import ct.buildcraft.transport.client.render.RenderPipeHolder;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import ct.buildcraft.transport.pipe.flow.PipeFlowFluids;
import ct.buildcraft.transport.pipe.flow.PipeFlowItems;
import ct.buildcraft.transport.pipe.flow.PipeFlowPower;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;

public class BCTransportModels {
   // public static final ResourceLocation BLOCKER_LOCATIOn = new ResourceLocation("buildcrafttransport:plugs/blocker");
  //  public static final ResourceLocation POWER_ADAPTER_LOCATION = new ResourceLocation("buildcrafttransport:plugs/power_adapter");

    public static final ModelHolderStatic BLOCKER;
    public static final ModelHolderStatic POWER_ADAPTER;
    
    private static final ModelHolderVariable STRIPES;
    private static final NodeVariableObject<Direction> STRIPES_DIRECTION;

    public static final IPluggableStaticBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER;
    public static final IPluggableStaticBaker<KeyPlugPowerAdaptor> BAKER_PLUG_POWER_ADAPTOR;

    static {
    	BLOCKER = getStaticModel("plugs/blocker");
    	POWER_ADAPTER = getStaticModel("plugs/power_adapter");

        BAKER_PLUG_BLOCKER = new PlugBakerSimple<>(BLOCKER::getCutoutQuads);
        BAKER_PLUG_POWER_ADAPTOR = new PlugBakerSimple<>(POWER_ADAPTER::getCutoutQuads);

        {
            FunctionContext fnCtx = DefaultContexts.createWithAll();
            STRIPES_DIRECTION = fnCtx.putVariableObject("side", Direction.class);
            STRIPES = getModel("pipes/stripes", fnCtx);
        }
    }

    private static ModelHolderStatic getStaticModel(String str) {
        return new ModelHolderStatic("buildcrafttransport:" + str);
    }

    private static ModelHolderVariable getModel(String str, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcrafttransport:models/" + str , fnCtx);
    }


    public static void init() {
        
        PipeApiClient.registry.registerBaker(KeyPlugBlocker.class, BAKER_PLUG_BLOCKER);
        PipeApiClient.registry.registerBaker(KeyPlugPowerAdaptor.class, BAKER_PLUG_POWER_ADAPTOR);

        PipeApiClient.registry.registerRenderer(PipeFlowItems.class, PipeFlowRendererItems.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowFluids.class, PipeFlowRendererFluids.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowPower.class, PipeFlowRendererPower.INSTANCE);

        PipeApiClient.registry.registerRenderer(PipeBehaviourStripes.class, PipeBehaviourRendererStripes.INSTANCE);
    }

    public static void onBlockEntityRender(EntityRenderersEvent.RegisterRenderers event) {
    	event.registerBlockEntityRenderer(BCTransportBlocks.PIPE_HOLDER_BE.get(), RenderPipeHolder::new);
    }
    
    public static void onBlockColor(RegisterColorHandlersEvent.Block event) {
    	event.register(PipeBlockColours.INSTANCE, BCTransportBlocks.pipeHolder.get());
    }
    
	public static void onModelBakePre(RegisterAdditional event) {
	//	event.register(BLOCKER_LOCATIOn);
	//	event.register(POWER_ADAPTER_LOCATION);
	}
	
    public static void onModelBake(BakingCompleted event) {
    	putModel(event, "pipe_holder", ModelPipe.INSTANCE);
    	putModel(event, "pipe_item#inventory", ModelPipeItem.INSTANCE);
//      putModel(event, "obsidian_item#inventory", ModelPipeItem.INSTANCE);

    	for(var item : BCTransportItems.PIPE_MAP.values()) 
    		putModel(event, item.getId().getPath() + "#inventory", ModelPipeItem.INSTANCE);
    	
  //  	BakedModel blocker = event.getModels().get(BLOCKER_LOCATIOn);
    //	BakedModel adaptor = event.getModels().get(POWER_ADAPTER_LOCATION);
    	
        PipeApiClient.registry.registerBaker(KeyPlugBlocker.class, BAKER_PLUG_BLOCKER);
        PipeApiClient.registry.registerBaker(KeyPlugPowerAdaptor.class, BAKER_PLUG_POWER_ADAPTOR);
        
        putModel(event, "plug_blocker#inventory", new ModelPluggableItem(BLOCKER.getCutoutQuads()));//new ModelPluggableItem(BLOCKER.getCutoutQuads()));
        putModel(event, "plug_power_adaptor#inventory", new ModelPluggableItem(POWER_ADAPTER.getCutoutQuads()));
    	
        PipeFlowRendererItems.onModelBake();

    }

    private static void putModel(BakingCompleted event, String str, BakedModel model) {
        event.getModels().put(new ModelResourceLocation("buildcrafttransport:" + str), model);
    }
    


    public static MutableQuad[] getStripesDynQuads(Direction side) {
        STRIPES_DIRECTION.value = side;
        return STRIPES.getCutoutQuads();
    }
}
