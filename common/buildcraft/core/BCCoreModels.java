/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.core.block.BlockEngine_BC8;
import buildcraft.core.client.render.RenderEngineCreative;
import buildcraft.core.client.render.RenderEngineWood;
import buildcraft.core.client.render.RenderMarkerVolume;
import buildcraft.core.tile.TileEngineCreative;
import buildcraft.core.tile.TileEngineRedstone_BC8;
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
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.LazyLoadedValue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class BCCoreModels {
    private static final NodeVariableDouble ENGINE_PROGRESS;
    private static final NodeVariableObject<EnumPowerStage> ENGINE_STAGE;
    private static final NodeVariableObject<Direction> ENGINE_FACING;

    private static final ModelHolderVariable ENGINE_REDSTONE;
    private static final ModelHolderVariable ENGINE_CREATIVE;

    static {
        FunctionContext fnCtx = new FunctionContext(ExpressionCompat.ENUM_POWER_STAGE, DefaultContexts.createWithAll());
        ENGINE_PROGRESS = fnCtx.putVariableDouble("progress");
        ENGINE_STAGE = fnCtx.putVariableObject("stage", EnumPowerStage.class);
        ENGINE_FACING = fnCtx.putVariableObject("direction", Direction.class);

        ENGINE_REDSTONE = new ModelHolderVariable(
//                "buildcraftcore:models/block/engine_redstone.json",
                "buildcraftcore:models/tile/engine_redstone.jsonbc",
                fnCtx
        );
        BlockEngine_BC8.setModel(EnumEngineType.WOOD, ENGINE_REDSTONE); // Calen
        ENGINE_CREATIVE = new ModelHolderVariable(
//                "buildcraftcore:models/block/engine_creative.json",
                "buildcraftcore:models/tile/engine_creative.jsonbc",
                fnCtx
        );
        BlockEngine_BC8.setModel(EnumEngineType.CREATIVE, ENGINE_CREATIVE); // Calen
    }

    public static void fmlPreInit() {
        // 1.18.2: following events are IModBusEvent
//        MinecraftForge.EVENT_BUS.register(BCCoreModels.class);
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(BCCore.MODID).get()).getEventBus();
        modEventBus.register(BCCoreModels.class);
    }

//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public static void onModelRegistry(ModelRegistryEvent event) {
//        if (BCCoreBlocks.engine != null) {
//            ModelLoader.setCustomStateMapper(BCCoreBlocks.engine, b -> Collections.emptyMap());
//        }
//    }

    // Calen: use onTesrReg(event)
//    public static void fmlInit() {
//        ClientRegistry.bindTileEntitySpecialRenderer(TileMarkerVolume.class, RenderMarkerVolume.INSTANCE);
//        ClientRegistry.bindTileEntitySpecialRenderer(TileEngineRedstone_BC8.class, RenderEngineWood.INSTANCE);
//        ClientRegistry.bindTileEntitySpecialRenderer(TileEngineCreative.class, RenderEngineCreative.INSTANCE);
//    }

    @SubscribeEvent
    public static void onTesrReg(RegisterRenderers event) {
        BlockEntityRenderers.register(BCCoreBlocks.markerVolumeTile.get(), RenderMarkerVolume::new);
        BlockEntityRenderers.register(BCCoreBlocks.engineWoodTile.get(), RenderEngineWood::new);
        BlockEntityRenderers.register(BCCoreBlocks.engineCreativeTile.get(), RenderEngineCreative::new);
    }

    // Calen 1.20.1
    private static final List<Runnable> spriteTasks = Lists.newLinkedList();

    // Calen 1.20.1
    @SubscribeEvent
    public static void onTextureStitchEvent$Post(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            spriteTasks.forEach(Runnable::run);
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        ENGINE_PROGRESS.value = 0.2;
        ENGINE_STAGE.value = EnumPowerStage.BLUE;
        ENGINE_FACING.value = Direction.UP;
        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(ENGINE_REDSTONE.createTickableNodes());
        varData.tick();
        varData.refresh();
        event.getModels().put(
//                new ModelResourceLocation(EnumEngineType.WOOD.getItemModelLocation(), "inventory"),
                new ModelResourceLocation(BCCoreBlocks.engineWood.getId(), "inventory"),
                new ModelItemSimple(
                        new LazyLoadedValue<>(
                                () -> Arrays.stream(ENGINE_REDSTONE.getCutoutQuads())
                                        .map(MutableQuad::toBakedItem)
                                        .collect(Collectors.toList())
                        ),
                        ModelItemSimple.TRANSFORM_BLOCK,
                        true,
                        spriteTasks::add
                )
        );
        ENGINE_STAGE.value = EnumPowerStage.BLACK;
        varData.setNodes(ENGINE_CREATIVE.createTickableNodes());
        varData.tick();
        varData.refresh();
        event.getModels().put(
//                new ModelResourceLocation(EnumEngineType.CREATIVE.getItemModelLocation(), "inventory"),
                new ModelResourceLocation(BCCoreBlocks.engineCreative.getId(), "inventory"),
                new ModelItemSimple(
                        new LazyLoadedValue<>(
                                () -> Arrays.stream(ENGINE_CREATIVE.getCutoutQuads())
                                        .map(MutableQuad::toBakedItem)
                                        .collect(Collectors.toList())
                        ),
                        ModelItemSimple.TRANSFORM_BLOCK,
                        true,
                        spriteTasks::add
                )
        );
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

    public static MutableQuad[] getRedstoneEngineQuads(TileEngineRedstone_BC8 tile, float partialTicks) {
        return getEngineQuads(ENGINE_REDSTONE, tile, partialTicks);
    }

    public static MutableQuad[] getCreativeEngineQuads(TileEngineCreative tile, float partialTicks) {
        return getEngineQuads(ENGINE_CREATIVE, tile, partialTicks);
    }
}
