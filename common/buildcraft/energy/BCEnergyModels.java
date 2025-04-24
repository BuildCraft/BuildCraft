/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.core.block.BlockEngine_BC8;
import buildcraft.energy.event.ChristmasHandler;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.ExpressionCompat;
import buildcraft.lib.misc.data.ModelVariableData;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import java.util.Arrays;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class BCEnergyModels {
    private static final NodeVariableDouble ENGINE_PROGRESS;
    private static final NodeVariableObject<EnumPowerStage> ENGINE_STAGE;
    private static final NodeVariableObject<Direction> ENGINE_FACING;

    private static final ModelHolderVariable ENGINE_STONE;
    private static final ModelHolderVariable ENGINE_IRON;
    private static final ModelHolderVariable ENGINE_RF;
    private static final ModelHolderVariable MJ_DYNAMO;

    static {
        FunctionContext fnCtx = new FunctionContext(ExpressionCompat.ENUM_POWER_STAGE, DefaultContexts.createWithAll());
        ENGINE_PROGRESS = fnCtx.putVariableDouble("progress");
        ENGINE_STAGE = fnCtx.putVariableObject("stage", EnumPowerStage.class);
        ENGINE_FACING = fnCtx.putVariableObject("direction", Direction.class);
        // TODO: Item models from "item/engine_stone.json"
        ENGINE_STONE = new ModelHolderVariable(
//                "buildcraftenergy:models/block/engine_stone.json",
                "buildcraftenergy:models/tiles/engine_stone.json",
                fnCtx
        );
        BlockEngine_BC8.setModel(EnumEngineType.STONE, ENGINE_STONE); // Calen
        ENGINE_IRON = new ModelHolderVariable(
//                "buildcraftenergy:models/block/engine_iron.json",
                "buildcraftenergy:models/tiles/engine_iron.json",
                fnCtx
        );
        BlockEngine_BC8.setModel(EnumEngineType.IRON, ENGINE_IRON); // Calen
        ENGINE_RF = new ModelHolderVariable(
                "buildcraftenergy:models/tiles/engine_rf.json",
                fnCtx
        );
        BlockEngine_BC8.setModel(EnumEngineType.RF, ENGINE_RF); // Calen
        MJ_DYNAMO = new ModelHolderVariable(
                "buildcraftenergy:models/tiles/mj_dynamo.json",
                fnCtx
        );
    }

    public static void fmlPreInit() {
        // 1.18.2: following events are IModBusEvent
//        MinecraftForge.EVENT_BUS.register(BCEnergyModels.class);
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(BCEnergy.MODID).get()).getEventBus();
        modEventBus.register(BCEnergyModels.class);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
//        for (BCFluid fluid : BCEnergyFluids.allFluids) {
//            ModelLoader.setCustomStateMapper(fluid.getBlock(), b -> Collections.emptyMap());
//        }
        ChristmasHandler.regBucketNoFlipModel(event);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        ENGINE_PROGRESS.value = 0.2;
        ENGINE_STAGE.value = EnumPowerStage.BLUE;
        ENGINE_FACING.value = Direction.UP;
        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(ENGINE_STONE.createTickableNodes());
        varData.tick();
        varData.refresh();
        event.getModelRegistry().put(
//                new ModelResourceLocation(EnumEngineType.STONE.getItemModelLocation(), "inventory"),
                new ModelResourceLocation(BCEnergyBlocks.engineStone.getId(), "inventory"),
                new ModelItemSimple(
                        Arrays.stream(ENGINE_STONE.getCutoutQuads())
                                .map(MutableQuad::toBakedItem)
                                .collect(Collectors.toList()),
                        ModelItemSimple.TRANSFORM_BLOCK,
                        true
                )
        );
        varData.setNodes(ENGINE_IRON.createTickableNodes());
        varData.tick();
        varData.refresh();
        event.getModelRegistry().put(
//                new ModelResourceLocation(EnumEngineType.IRON.getItemModelLocation(), "inventory"),
                new ModelResourceLocation(BCEnergyBlocks.engineIron.getId(), "inventory"),
                new ModelItemSimple(
                        Arrays.stream(ENGINE_IRON.getCutoutQuads())
                                .map(MutableQuad::toBakedItem)
                                .collect(Collectors.toList()),
                        ModelItemSimple.TRANSFORM_BLOCK,
                        true
                )
        );
        varData.setNodes(ENGINE_RF.createTickableNodes());
        varData.tick();
        varData.refresh();
        event.getModelRegistry().put(
                // new ModelResourceLocation(EnumEngineType.RF.getItemModelLocation(), "inventory"),
                new ModelResourceLocation(BCEnergyBlocks.engineRf.getId(), "inventory"),
                new ModelItemSimple(
                        Arrays.stream(ENGINE_RF.getCutoutQuads())
                                .map(MutableQuad::toBakedItem)
                                .collect(Collectors.toList()),
                        ModelItemSimple.TRANSFORM_BLOCK,
                        true
                )
        );
        varData.setNodes(MJ_DYNAMO.createTickableNodes());
        varData.tick();
        varData.refresh();
        event.getModelRegistry().put(
                new ModelResourceLocation(BCEnergyBlocks.mjDynamo.getId(), "inventory"),
                new ModelItemSimple(
                        Arrays.stream(MJ_DYNAMO.getCutoutQuads())
                                .map(MutableQuad::toBakedItem)
                                .collect(Collectors.toList()),
                        ModelItemSimple.TRANSFORM_BLOCK,
                        true
                )
        );

        ChristmasHandler.replaceBucketNoFlipModel(event);
    }

    private static MutableQuad[] getEngineQuads(ModelHolderVariable model,
            TileEngineBase_BC8 tile,
            float partialTicks) {
        ENGINE_PROGRESS.value = tile.getProgressClient(partialTicks);
        ENGINE_STAGE.value = tile.getPowerStage();
        ENGINE_FACING.value = tile.getCurrentFacing();
        if (tile.clientModelData.hasNoNodes()) {
            tile.clientModelData.setNodes(model.createTickableNodes());
        }
        tile.clientModelData.refresh();
        return model.getCutoutQuads();
    }

    public static MutableQuad[] getStoneEngineQuads(TileEngineStone_BC8 tile, float partialTicks) {
        return getEngineQuads(ENGINE_STONE, tile, partialTicks);
    }

    public static MutableQuad[] getIronEngineQuads(TileEngineIron_BC8 tile, float partialTicks) {
        return getEngineQuads(ENGINE_IRON, tile, partialTicks);
    }

    public static MutableQuad[] getRfEngineQuads(TileEngineRF tile, float partialTicks) {
        return getEngineQuads(ENGINE_RF, tile, partialTicks);
    }

    public static MutableQuad[] getMjDynamoQuads(TileDynamoMJ tile, float partialTicks) {
        ENGINE_PROGRESS.value = tile.getProgressClient(partialTicks);
        ENGINE_STAGE.value = tile.getPowerStage();
        ENGINE_FACING.value = tile.getCurrentDirection();
        if (tile.clientModelData.hasNoNodes()) {
            tile.clientModelData.setNodes(MJ_DYNAMO.createTickableNodes());
        }
        tile.clientModelData.refresh();
        return MJ_DYNAMO.getCutoutQuads();
    }
}
