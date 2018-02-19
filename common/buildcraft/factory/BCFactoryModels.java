/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import buildcraft.api.BCBlocks;
import buildcraft.factory.client.model.ModelHeatExchange;
import buildcraft.factory.client.render.*;
import buildcraft.factory.tile.*;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.expression.FunctionContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BCFactoryModels {
    public static final ModelHolderVariable DISTILLER;
    public static final ModelHolderVariable HEAT_EXCHANGE_STATIC;

    static {
        DISTILLER = getModel("tiles/distiller.json", TileDistiller_BC8.MODEL_FUNC_CTX);
        HEAT_EXCHANGE_STATIC = getModel("tiles/heat_exchange_static.json", ModelHeatExchange.FUNCTION_CONTEXT);
    }

    private static ModelHolderVariable getModel(String loc, FunctionContext fnCtx) {
        return new ModelHolderVariable("buildcraftfactory:models/" + loc, fnCtx);
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCFactoryModels.class);
    }

    public static void fmlInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMiningWell.class, new RenderMiningWell());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePump.class, new RenderPump());
        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileDistiller_BC8.class, new RenderDistiller());
        ClientRegistry.bindTileEntitySpecialRenderer(TileHeatExchange.class, new RenderHeatExchange());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        event.getModelManager().getBlockModelShapes().registerBlockWithStateMapper(BCBlocks.Factory.HEAT_EXCHANGE,
            new StateMapperBase() {
                @Override
                protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation("buildcraftfactory:heat_exchange#normal");
                }
            });
        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        String start = "buildcraftfactory:";

        ModelHeatExchange fullModel = new ModelHeatExchange();
        registerModel(modelRegistry, start + "heat_exchange#normal", fullModel);

        ModelItemSimple itemModel = new ModelItemSimple(fullModel.itemQuads, ModelItemSimple.TRANSFORM_BLOCK, true);
        registerModel(modelRegistry, start + "heat_exchange#inventory", itemModel);
    }

    private static void registerModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String reg,
        IBakedModel val) {
        modelRegistry.putObject(new ModelResourceLocation(reg), val);
    }
}
