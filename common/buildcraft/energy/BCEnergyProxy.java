/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.energy.client.gui.GuiDynamoMJ;
import buildcraft.energy.client.gui.GuiEngineIron_BC8;
import buildcraft.energy.client.gui.GuiEngineRF;
import buildcraft.energy.client.gui.GuiEngineStone_BC8;
import buildcraft.energy.client.render.RenderDynamoMJ;
import buildcraft.energy.client.render.RenderEngineIron;
import buildcraft.energy.client.render.RenderEngineRF;
import buildcraft.energy.client.render.RenderEngineStone;
import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.energy.container.ContainerEngineIron_BC8;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.energy.event.ChristmasHandler;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.energy.tile.TileEngineStone_BC8;

public abstract class BCEnergyProxy implements IGuiHandler {
    @SidedProxy(modId = BCEnergy.MODID)
    private static BCEnergyProxy proxy;

    public static BCEnergyProxy getProxy() {
        return proxy;
    }

    public void fmlPreInit() {}

    public void fmlInit() {}

    public void fmlPostInit() {}

    @Override
    public Object getClientGuiElement(int id, PlayerEntity player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getServerGuiElement(int id, PlayerEntity player, World world, int x, int y, int z) {
        BCEnergyGuis gui = BCEnergyGuis.get(id);
        if (gui == null) return null;
        BlockPos pos = new BlockPos(x, y, z);
        BlockEntity tile = world.getBlockEntity(pos);
        switch (gui) {
            case ENGINE_STONE:
                if (tile instanceof TileEngineStone_BC8) {
                    return new ContainerEngineStone_BC8(player, (TileEngineStone_BC8) tile);
                }
                return null;
            case ENGINE_IRON:
                if (tile instanceof TileEngineIron_BC8) {
                    return new ContainerEngineIron_BC8(player, (TileEngineIron_BC8) tile);
                }

                return null;
            case ENGINE_RF:
                if (tile instanceof TileEngineRF) {
                    return new ContainerEngineRF(player, (TileEngineRF) tile);
                }

                return null;
            case DYNAMO_MJ:
                if (tile instanceof TileDynamoMJ) {
                    return new ContainerDynamoMJ(player, (TileDynamoMJ) tile);
                }

                return null;
            default:
                return null;
        }
    }

    @Environment(EnvType.SERVER)
    public static class ServerProxy extends BCEnergyProxy {
        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            ChristmasHandler.fmlPreInitDedicatedServer();
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ClientProxy extends BCEnergyProxy {
        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            ChristmasHandler.fmlPreInitClient();
            BCEnergyModels.fmlPreInit();
            BCEnergySprites.fmlPreInit();
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            ClientRegistry.bindTileEntitySpecialRenderer(TileEngineStone_BC8.class, RenderEngineStone.INSTANCE);
            ClientRegistry.bindTileEntitySpecialRenderer(TileEngineIron_BC8.class, RenderEngineIron.INSTANCE);
            ClientRegistry.bindTileEntitySpecialRenderer(TileEngineRF.class, RenderEngineRF.INSTANCE);
            ClientRegistry.bindTileEntitySpecialRenderer(TileDynamoMJ.class, RenderDynamoMJ.INSTANCE);
        }

        @Override
        public Object getClientGuiElement(int id, PlayerEntity player, World world, int x, int y, int z) {
            BCEnergyGuis gui = BCEnergyGuis.get(id);
            if (gui == null) return null;
            BlockPos pos = new BlockPos(x, y, z);
            BlockEntity tile = world.getBlockEntity(pos);
            switch (gui) {
                case ENGINE_STONE:
                    if (tile instanceof TileEngineStone_BC8) {
                        return new GuiEngineStone_BC8(new ContainerEngineStone_BC8(player, (TileEngineStone_BC8) tile));
                    }
                    return null;
                case ENGINE_IRON:
                    if (tile instanceof TileEngineIron_BC8) {
                        return new GuiEngineIron_BC8(new ContainerEngineIron_BC8(player, (TileEngineIron_BC8) tile));
                    }
                    return null;
                case ENGINE_RF:
                    if (tile instanceof TileEngineRF) {
                        return new GuiEngineRF(new ContainerEngineRF(player, (TileEngineRF) tile));
                    }
                    return null;
                case DYNAMO_MJ:
                    if (tile instanceof TileDynamoMJ) {
                        return new GuiDynamoMJ(new ContainerDynamoMJ(player, (TileDynamoMJ) tile));
                    }
                    return null;
                default:
                    return null;
            }
        }
    }
}
