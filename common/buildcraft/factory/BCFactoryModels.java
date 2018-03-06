/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;

import buildcraft.factory.client.model.ModelHeatExchange;
import buildcraft.factory.client.render.RenderDistiller;
import buildcraft.factory.client.render.RenderHeatExchange;
import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;
import buildcraft.factory.client.render.RenderTank;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileTank;

public class BCFactoryModels {
    public static final ModelHolderVariable DISTILLER = new ModelHolderVariable(
        "buildcraftfactory:models/tiles/distiller.json",
        TileDistiller_BC8.MODEL_FUNC_CTX
    );
    public static final ModelHolderVariable HEAT_EXCHANGE_STATIC = new ModelHolderVariable(
        "buildcraftfactory:models/tiles/heat_exchange_static.json",
        ModelHeatExchange.FUNCTION_CONTEXT
    );

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCFactoryModels.class);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (BCFactoryBlocks.heatExchange != null) {
            ModelLoader.setCustomStateMapper(
                BCFactoryBlocks.heatExchange,
                new StateMapperBase() {
                    @Nonnull
                    @Override
                    protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
                        return new ModelResourceLocation("buildcraftfactory:heat_exchange#normal");
                    }
                }
            );
        }
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
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcraftfactory:heat_exchange#normal"),
            new ModelHeatExchange()
        );
        event.getModelRegistry().putObject(
            new ModelResourceLocation("buildcraftfactory:heat_exchange#inventory"),
            new ModelItemSimple(
                Arrays.stream(BCFactoryModels.HEAT_EXCHANGE_STATIC.getCutoutQuads())
                    .map(MutableQuad::multShade)
                    .map(MutableQuad::toBakedItem)
                    .collect(Collectors.toList()),
                ModelItemSimple.TRANSFORM_BLOCK,
                true
            )
        );
    }
}
