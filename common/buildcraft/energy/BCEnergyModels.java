/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelFluid;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumPowerStage;

import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.misc.ExpressionCompat;
import buildcraft.lib.misc.data.ModelVariableData;

import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineStone_BC8;

public class BCEnergyModels {
    private static final NodeVariableDouble ENGINE_PROGRESS;
    private static final NodeVariableObject<EnumPowerStage> ENGINE_STAGE;
    private static final NodeVariableObject<EnumFacing> ENGINE_FACING;

    private static final ModelHolderVariable ENGINE_STONE;
    private static final ModelHolderVariable ENGINE_IRON;

    static {
        FunctionContext fnCtx = new FunctionContext(ExpressionCompat.ENUM_POWER_STAGE, DefaultContexts.createWithAll());
        ENGINE_PROGRESS = fnCtx.putVariableDouble("progress");
        ENGINE_STAGE = fnCtx.putVariableObject("stage", EnumPowerStage.class);
        ENGINE_FACING = fnCtx.putVariableObject("direction", EnumFacing.class);
        // TODO: Item models from "item/engine_stone.json"
        ENGINE_STONE = new ModelHolderVariable(
            "buildcraftenergy:models/block/engine_stone.json",
            fnCtx
        );
        ENGINE_IRON = new ModelHolderVariable(
            "buildcraftenergy:models/block/engine_iron.json",
            fnCtx
        );
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCEnergyModels.class);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (BCFluid fluid : BCEnergyFluids.allFluids) {
            ModelLoader.setCustomStateMapper(fluid.getBlock(), b -> Collections.emptyMap());
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        ENGINE_PROGRESS.value = 0.2;
        ENGINE_STAGE.value = EnumPowerStage.BLUE;
        ENGINE_FACING.value = EnumFacing.UP;
        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(ENGINE_STONE.createTickableNodes());
        varData.tick();
        varData.refresh();
        event.getModelRegistry().putObject(
            new ModelResourceLocation(EnumEngineType.STONE.getItemModelLocation(), "inventory"),
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
        event.getModelRegistry().putObject(
            new ModelResourceLocation(EnumEngineType.IRON.getItemModelLocation(), "inventory"),
            new ModelItemSimple(
                Arrays.stream(ENGINE_IRON.getCutoutQuads())
                    .map(MutableQuad::toBakedItem)
                    .collect(Collectors.toList()),
                ModelItemSimple.TRANSFORM_BLOCK,
                true
            )
        );
        for (BCFluid fluid : BCEnergyFluids.allFluids) {
            ModelFluid modelFluid = new ModelFluid(fluid);
            event.getModelRegistry().putObject(
                new ModelResourceLocation("buildcraftenergy:fluid_block_" + fluid.getBlockName()),
                modelFluid.bake(
                    modelFluid.getDefaultState(),
                    DefaultVertexFormats.ITEM,
                    ModelLoader.defaultTextureGetter()
                )
            );
        }
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
}
