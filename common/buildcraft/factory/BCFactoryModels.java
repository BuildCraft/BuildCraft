/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import buildcraft.factory.client.model.ModelHeatExchange;
import buildcraft.factory.client.render.*;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.ExpressionCompat;
import buildcraft.lib.misc.RegistryUtil;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BCFactoryModels {
    public static final ModelHolderVariable DISTILLER;
    public static final ModelHolderVariable HEAT_EXCHANGE_STATIC;

    static {
        // Calen: ensure ExpressionCompat ENUM_FACING = new NodeType<>("Facing", Direction.UP) runned
        // or this will cause IllegalArgumentException: Unknown NodeType class net.minecraft.util.Direction
        ExpressionCompat.setup();

        DISTILLER = new ModelHolderVariable(
                "buildcraftfactory:models/tiles/distiller.json",
                TileDistiller_BC8.MODEL_FUNC_CTX
        );
        HEAT_EXCHANGE_STATIC = new ModelHolderVariable(
                "buildcraftfactory:models/tiles/heat_exchange_static.json",
                ModelHeatExchange.FUNCTION_CONTEXT
        );
    }

    public static void fmlPreInit() {
        // 1.18.2: following events are IModBusEvent
//        MinecraftForge.EVENT_BUS.register(BCFactoryModels.class);
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(BCFactory.MODID).get()).getEventBus();
        modEventBus.register(BCFactoryModels.class);
    }

//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public static void onModelRegistry(ModelRegistryEvent event) {
//        if (BCFactoryBlocks.heatExchange != null) {
//            ModelLoader.setCustomStateMapper(
//                    BCFactoryBlocks.heatExchange,
//                    new StateMapperBase() {
//                        @Nonnull
//                        @Override
//                        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
//                            return new ModelResourceLocation("buildcraftfactory:heat_exchange#normal");
//                        }
//                    }
//            );
//        }
//    }

    public static void fmlInit() {
//        ClientRegistry.bindTileEntitySpecialRenderer(TileMiningWell.class, new RenderMiningWell());
        RegistryUtil.regTesrIfTilePresent(BCFactoryBlocks.miningWellTile, RenderMiningWell::new);
//        ClientRegistry.bindTileEntitySpecialRenderer(TilePump.class, new RenderPump());
        RegistryUtil.regTesrIfTilePresent(BCFactoryBlocks.pumpTile, RenderPump::new);
//        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
        RegistryUtil.regTesrIfTilePresent(BCFactoryBlocks.tankTile, RenderTank::new);
//        ClientRegistry.bindTileEntitySpecialRenderer(TileDistiller_BC8.class, new RenderDistiller());
        RegistryUtil.regTesrIfTilePresent(BCFactoryBlocks.distillerTile, RenderDistiller::new);
//        ClientRegistry.bindTileEntitySpecialRenderer(TileHeatExchange.class, new RenderHeatExchange());
        RegistryUtil.regTesrIfTilePresent(BCFactoryBlocks.heatExchangeTile, RenderHeatExchange::new);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onModelBake(ModelBakeEvent event) {
        // Calen: to set model for each blockState
        // the model path contains blockstate props
        ModelHeatExchange modelHeatExchange = new ModelHeatExchange();
        event.getModelRegistry().replaceAll((rl, m) -> (
                rl.getNamespace().equals(BCFactory.MODID)
                        && rl.getPath().contains("heat_exchange")
                        && !rl.getPath().contains("inventory")
        ) ? modelHeatExchange : m);
        event.getModelRegistry().replace(
                new ModelResourceLocation(BCFactoryBlocks.heatExchange.getId(), "inventory"),
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
