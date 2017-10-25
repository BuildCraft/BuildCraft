/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.MessageManager.MessageId;

import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import buildcraft.transport.container.ContainerGate;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiDiamondWoodPipe;
import buildcraft.transport.gui.GuiEmzuliPipe_BC8;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.gui.GuiGate;
import buildcraft.transport.item.ItemPluggableFacade;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.plug.FacadeInstance;
import buildcraft.transport.plug.FacadePhasedState;
import buildcraft.transport.plug.PluggableGate;
import buildcraft.transport.tile.TileFilteredBuffer;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.MessageWireSystems;
import buildcraft.transport.wire.MessageWireSystemsPowered;

public abstract class BCTransportProxy implements IGuiHandler {
    @SidedProxy(modId = BCTransport.MODID)
    private static BCTransportProxy proxy;

    public static BCTransportProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BCTransportGuis gui = BCTransportGuis.get(id);
        if (gui == null) return null;
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

        switch (gui) {
            case FILTERED_BUFFER: {
                if (tile instanceof TileFilteredBuffer) {
                    TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                    return new ContainerFilteredBuffer_BC8(player, filteredBuffer);
                }
                break;
            }
            case PIPE_DIAMOND: {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourDiamond) {
                        PipeBehaviourDiamond diaPipe = (PipeBehaviourDiamond) behaviour;
                        return new ContainerDiamondPipe(player, diaPipe);
                    }
                }
                break;
            }
            case PIPE_DIAMOND_WOOD: {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourWoodDiamond) {
                        PipeBehaviourWoodDiamond diaPipe = (PipeBehaviourWoodDiamond) behaviour;
                        return new ContainerDiamondWoodPipe(player, diaPipe);
                    }
                }
                break;
            }
            case PIPE_EMZULI: {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourEmzuli) {
                        PipeBehaviourEmzuli emPipe = (PipeBehaviourEmzuli) behaviour;
                        return new ContainerEmzuliPipe_BC8(player, emPipe);
                    }
                }
                break;
            }
            case GATE: {
                int ry = y >> 3;
                EnumFacing gateSide = EnumFacing.getFront(y & 0x7);
                tile = world.getTileEntity(new BlockPos(x, ry, z));
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    PipePluggable plug = holder.getPluggable(gateSide);
                    if (plug instanceof PluggableGate) {
                        return new ContainerGate(player, ((PluggableGate) plug).logic);
                    }
                }
                break;
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {}

    public void fmlInit() {}

    public void fmlPostInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCTransportProxy {

        @Override
        public void fmlPreInit() {
            super.fmlPreInit();

            MessageManager.addTypeSent(MessageId.BC_SILICON_WIRE_NETWORK, MessageWireSystems.class, Side.CLIENT);
            MessageManager.addTypeSent(MessageId.BC_SILICON_WIRE_SWITCH, MessageWireSystemsPowered.class, Side.CLIENT);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCTransportProxy {
        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            BCTransportSprites.fmlPreInit();
            BCTransportModels.fmlPreInit();
            PipeApiClient.registry = PipeRegistryClient.INSTANCE;

            MessageManager.addType(MessageId.BC_SILICON_WIRE_NETWORK, MessageWireSystems.class, MessageWireSystems.HANDLER, Side.CLIENT);
            MessageManager.addType(MessageId.BC_SILICON_WIRE_SWITCH, MessageWireSystemsPowered.class, MessageWireSystemsPowered.HANDLER, Side.CLIENT);
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            BCTransportModels.fmlInit();
        }

        @Override
        public void fmlPostInit() {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
                if (world != null && pos != null) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof TilePipeHolder) {
                        TilePipeHolder tilePipeHolder = (TilePipeHolder) tile;
                        EnumFacing side = EnumFacing.getFront(tintIndex % EnumFacing.VALUES.length);
                        PipePluggable pluggable = tilePipeHolder.getPluggable(side);
                        if (pluggable != null) {
                            return pluggable.getBlockColor(tintIndex / 6);
                        }
                    }
                }
                return -1;
            }, BCTransportBlocks.pipeHolder);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler((item, tintIndex) -> {
                FacadeInstance states = ItemPluggableFacade.getStates(item);
                FacadePhasedState state = states.getCurrentStateForStack();
                return Minecraft.getMinecraft().getBlockColors().getColor(state.stateInfo.state);
            }, BCTransportItems.plugFacade);
        }

        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            BCTransportGuis gui = BCTransportGuis.get(id);
            if (gui == null) {
                return null;
            }
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            switch (gui) {
                case FILTERED_BUFFER: {
                    if (tile instanceof TileFilteredBuffer) {
                        TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                        return new GuiFilteredBuffer(new ContainerFilteredBuffer_BC8(player, filteredBuffer));
                    }
                    break;
                }
                case PIPE_DIAMOND: {
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IPipe pipe = holder.getPipe();
                        if (pipe == null) return null;
                        PipeBehaviour behaviour = pipe.getBehaviour();
                        if (behaviour instanceof PipeBehaviourDiamond) {
                            PipeBehaviourDiamond diaPipe = (PipeBehaviourDiamond) behaviour;
                            return new GuiDiamondPipe(player, diaPipe);
                        }
                    }
                    break;
                }
                case PIPE_DIAMOND_WOOD: {
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IPipe pipe = holder.getPipe();
                        if (pipe == null) return null;
                        PipeBehaviour behaviour = pipe.getBehaviour();
                        if (behaviour instanceof PipeBehaviourWoodDiamond) {
                            PipeBehaviourWoodDiamond diaPipe = (PipeBehaviourWoodDiamond) behaviour;
                            return new GuiDiamondWoodPipe(player, diaPipe);
                        }
                    }
                    break;
                }
                case PIPE_EMZULI: {
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IPipe pipe = holder.getPipe();
                        if (pipe == null) return null;
                        PipeBehaviour behaviour = pipe.getBehaviour();
                        if (behaviour instanceof PipeBehaviourEmzuli) {
                            PipeBehaviourEmzuli emzPipe = (PipeBehaviourEmzuli) behaviour;
                            return new GuiEmzuliPipe_BC8(player, emzPipe);
                        }
                    }
                    break;
                }
                case GATE: {
                    int ry = y >> 3;
                    EnumFacing gateSide = EnumFacing.getFront(y & 0x7);
                    tile = world.getTileEntity(new BlockPos(x, ry, z));
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        PipePluggable plug = holder.getPluggable(gateSide);
                        if (plug instanceof PluggableGate) {
                            return new GuiGate(new ContainerGate(player, ((PluggableGate) plug).logic));
                        }
                    }
                    break;
                }
            }
            return null;
        }
    }
}
