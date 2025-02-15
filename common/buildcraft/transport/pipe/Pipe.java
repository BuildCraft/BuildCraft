/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.client.model.key.PipeModelKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;

public final class Pipe implements IPipe, IDebuggable {
    private static final float DEFAULT_CONNECTION_DISTANCE = 0.25f;

    public final IPipeHolder holder;
    public final PipeDefinition definition;
    public final PipeBehaviour behaviour;
    public final PipeFlow flow;
    private DyeColor colour = null;
    private boolean updateMarked = true;
    private final EnumMap<Direction, Float> connected = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, ConnectedType> types = new EnumMap<>(Direction.class);

    @OnlyIn(Dist.CLIENT)
    private PipeModelKey lastModel;

    public Pipe(IPipeHolder holder, PipeDefinition definition) {
        this.holder = holder;
        this.definition = definition;
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        this.flow = definition.flowType.creator.createFlow(this);
    }

    // read + write

    public Pipe(IPipeHolder holder, CompoundTag nbt) throws InvalidInputDataException {
        this.holder = holder;
        this.colour = NBTUtilBC.readEnum(nbt.get("col"), DyeColor.class);
        this.definition = PipeRegistry.INSTANCE.loadDefinition(nbt.getString("def"));
        if (!definition.canBeColoured) {
            colour = null;
        }
        this.behaviour = definition.logicLoader.loadBehaviour(this, nbt.getCompound("beh"));
        this.flow = definition.flowType.loader.loadFlow(this, nbt.getCompound("flow"));

        int connectionData = nbt.getInt("con");
        for (Direction face : Direction.VALUES) {
            int data = (connectionData >>> (face.ordinal() * 2)) & 0b11;
            // The only important aspect of this is the pipe type
            // as the texture index is just used at the client (which is updated in the first tick)
            // and the distance is only used on the server for item pipe travel times.
            // (which is minor enough that it doesn't really matter)
            if (data == 0b01) {
                connected.put(face, DEFAULT_CONNECTION_DISTANCE);
                types.put(face, ConnectedType.PIPE);
            } else if (data == 0b10) {
                connected.put(face, DEFAULT_CONNECTION_DISTANCE);
                types.put(face, ConnectedType.TILE);
            }
        }
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("col", NBTUtilBC.writeEnum(colour));
        nbt.putString("def", definition.identifier.toString());
        nbt.put("beh", behaviour.writeToNbt());
        nbt.put("flow", flow.writeToNbt());

        int connectionData = 0;
        for (Direction face : Direction.VALUES) {
            ConnectedType type = types.get(face);
            if (type != null) {
                int data = type == ConnectedType.PIPE ? 0b01 : 0b10;
                connectionData |= data << (face.ordinal() * 2);
            }
        }
        nbt.putInt("con", connectionData);
        return nbt;
    }

    // network

    public Pipe(IPipeHolder holder, PacketBufferBC buffer, NetworkEvent.Context ctx) throws IOException {
        this.holder = holder;
        try {
            this.definition = PipeRegistry.INSTANCE.loadDefinition(buffer.readUtf(256));
        } catch (InvalidInputDataException e) {
            throw new IOException(e);
        }
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        readPayload(buffer, NetworkDirection.PLAY_TO_CLIENT, ctx);
        this.flow = definition.flowType.creator.createFlow(this);
        this.flow.readPayload(PipeFlow.NET_ID_FULL_STATE, buffer, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void writeCreationPayload(PacketBufferBC buffer) {
        buffer.writeUtf(definition.identifier.toString());
        writePayload(buffer, Dist.DEDICATED_SERVER);
        flow.writePayload(PipeFlow.NET_ID_FULL_STATE, buffer, Dist.DEDICATED_SERVER);
    }

    public void writePayload(PacketBufferBC buffer, Dist side) {
        if (side == Dist.DEDICATED_SERVER) {
            buffer.writeByte(colour == null ? 0 : colour.getId() + 1);
            for (Direction face : Direction.VALUES) {
                Float con = connected.get(face);
                if (con != null) {
                    buffer.writeBoolean(true);
                    buffer.writeFloat(con);
                    MessageUtil.writeEnumOrNull(buffer, types.get(face));
                } else {
                    buffer.writeBoolean(false);
                }
            }
            behaviour.writePayload(buffer, side);
        }
    }

    @OnlyIn(Dist.CLIENT)
//    public void readPayload(PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            connected.clear();
            types.clear();

            int nColour = buffer.readUnsignedByte();
            colour = nColour == 0 ? null : DyeColor.byId(nColour - 1);

            for (Direction face : Direction.VALUES) {
                if (buffer.readBoolean()) {
                    float dist = buffer.readFloat();

                    connected.put(face, dist);

                    ConnectedType type = MessageUtil.readEnumOrNull(buffer, ConnectedType.class);
                    types.put(face, type);
                }
            }

            behaviour.readPayload(buffer, side, ctx);

//            PipeModelKey model = getModel();
//            if (!model.equals(lastModel)) {
//                lastModel = model;
//                getHolder().scheduleRenderUpdate();
//            }
            // Calen: when world loading, TilePipeHolder#getNeighbourTile -> [lib.tile] Ghost-loading tile at ...
            getHolder().runWhenWorldNotNull(
                    () ->
                    {
                        PipeModelKey model = getModel();
                        if (!model.equals(lastModel)) {
                            lastModel = model;
                            getHolder().scheduleRenderUpdate();
                        }
                    },
                    true
            );
        }
    }

    // IPipe

    @Override
    public IPipeHolder getHolder() {
        return holder;
    }

    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }

    @Override
    public PipeBehaviour getBehaviour() {
        return behaviour;
    }

    @Override
    public PipeFlow getFlow() {
        return flow;
    }

    @Override
    public DyeColor getColour() {
        return this.colour;
    }

    @Override
    public void setColour(DyeColor colour) {
        if (definition.canBeColoured) {
            this.colour = colour;
            markForUpdate();
        }
    }

    // Caps

//    @Override
//    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
//        return getCapability(capability, facing) != null;
//    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        LazyOptional<T> val = behaviour.getCapability(capability, facing);
        if (val.isPresent()) return val;
        return flow.getCapability(capability, facing);
    }

    // misc

    public void onLoad() {
        markForUpdate();
    }

    public void onTick() {
        if (updateMarked) {
            // Ensure that the behaviour and flow *always* get valid connection data
            // (for example if we just read from disk)
            updateConnections();
        }
        behaviour.onTick();
        flow.onTick();
        if (updateMarked) {
            updateConnections();
        }
    }

    private void updateConnections() {
        if (holder.getPipeWorld().isClientSide) {
            return;
        }
        updateMarked = false;

        EnumMap<Direction, Float> old = connected.clone();

        connected.clear();
        types.clear();

        for (Direction facing : Direction.VALUES) {
            PipePluggable plug = getHolder().getPluggable(facing);
            if (plug != null && plug.isBlocking()) {
                continue;
            }
            BlockEntity oTile = getHolder().getNeighbourTile(facing);
            if (oTile == null) {
                continue;
            }
            IPipe oPipe = getHolder().getNeighbourPipe(facing);
            if (oPipe != null) {
                PipeBehaviour oBehaviour = oPipe.getBehaviour();
                if (oBehaviour == null) {
                    continue;
                }
//                PipePluggable oPlug = oTile.getCapability(PipeApi.CAP_PLUG, facing.getOpposite());
                PipePluggable oPlug = oTile.getCapability(PipeApi.CAP_PLUG, facing.getOpposite()).orElse(null);
                if (oPlug == null || !oPlug.isBlocking()) {
                    if (canPipesConnect(facing, this, oPipe)) {
                        connected.put(facing, DEFAULT_CONNECTION_DISTANCE);
                        types.put(facing, ConnectedType.PIPE);
                    }
                    continue;
                }
            }

            BlockPos nPos = holder.getPipePos().relative(facing);
            BlockState neighbour = holder.getPipeWorld().getBlockState(nPos);

            ICustomPipeConnection cust = PipeConnectionAPI.getCustomConnection(neighbour.getBlock());
            if (cust == null) {
                cust = DefaultPipeConnection.INSTANCE;
            }
            float ext = DEFAULT_CONNECTION_DISTANCE
                    + cust.getExtension(holder.getPipeWorld(), nPos, facing.getOpposite(), neighbour);

            if (behaviour.shouldForceConnection(facing, oTile) || flow.shouldForceConnection(facing, oTile)
                    || (behaviour.canConnect(facing, oTile) && flow.canConnect(facing, oTile)))
            {
                connected.put(facing, ext);
                types.put(facing, ConnectedType.TILE);
            }
        }
        if (!old.equals(connected)) {
            for (Direction face : Direction.VALUES) {
                boolean o = old.containsKey(face);
                boolean n = connected.containsKey(face);
                if (o != n) {
                    IPipe oPipe = getHolder().getNeighbourPipe(face);
                    if (oPipe != null) {
                        oPipe.markForUpdate();
                    }
                    holder.fireEvent(new PipeEventConnectionChange(holder, face));
                }
            }
        }
        getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
    }

    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        // Calen: reg different item objects for different colours
//        Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(definition);
        Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(definition, colour);
        if (item != null) {
//            toDrop.add(new ItemStack(item, 1, colour == null ? 0 : 1 + colour.ordinal()));
            toDrop.add(new ItemStack(item, 1));
        }
        flow.addDrops(toDrop, fortune);
        behaviour.addDrops(toDrop, fortune);
    }

    public static boolean canPipesConnect(Direction to, IPipe one, IPipe two) {
        return canColoursConnect(one.getColour(), two.getColour())//
                && canBehavioursConnect(to, one.getBehaviour(), two.getBehaviour())//
                && canFlowsConnect(to, one.getFlow(), two.getFlow());
    }

    public static boolean canColoursConnect(DyeColor one, DyeColor two) {
        return one == null || two == null || one == two;
    }

    public static boolean canBehavioursConnect(Direction to, PipeBehaviour one, PipeBehaviour two) {
        return one.canConnect(to, two) && two.canConnect(to.getOpposite(), one);
    }

    public static boolean canFlowsConnect(Direction to, PipeFlow one, PipeFlow two) {
        return one.canConnect(to, two) && two.canConnect(to.getOpposite(), one);
    }

    @Override
    public void markForUpdate() {
        updateMarked = true;
    }

    @OnlyIn(Dist.CLIENT)
    public PipeModelKey getModel() {
        PipeFaceTex[] sides = new PipeFaceTex[6];
        float[] mc = new float[6];
        for (Direction face : Direction.VALUES) {
            int i = face.ordinal();
            sides[i] = behaviour.getTextureData(face);
            mc[i] = getConnectedDist(face);
        }
        return new PipeModelKey(definition, behaviour.getTextureData(null), sides, mc, colour);
    }

    @Override
    public BlockEntity getConnectedTile(Direction side) {
        if (connected.containsKey(side)) {
            BlockEntity offset = getHolder().getNeighbourTile(side);
            if (offset == null && !getHolder().getPipeWorld().isClientSide) {
                markForUpdate();
            } else {
                return offset;
            }
        }
        return null;
    }

    @Override
    public IPipe getConnectedPipe(Direction side) {
        if (connected.containsKey(side) && getConnectedType(side) == ConnectedType.PIPE) {
            IPipe offset = getHolder().getNeighbourPipe(side);
            if (offset == null && !getHolder().getPipeWorld().isClientSide) {
                markForUpdate();
            } else {
                return offset;
            }
        }
        return null;
    }

    @Override
    public ConnectedType getConnectedType(Direction side) {
        return types.get(side);
    }

    @Override
    public boolean isConnected(Direction side) {
        return connected.containsKey(side);
    }

    public float getConnectedDist(Direction face) {
        Float custom = connected.get(face);
        return custom == null ? 0 : custom;
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//        left.add("Colour = " + colour);
        left.add(Component.literal("Colour = " + colour));
//        left.add("Definition = " + definition.identifier);
        left.add(Component.literal("Definition = " + definition.identifier));
        if (behaviour instanceof IDebuggable) {
//            left.add("Behaviour:");
            left.add(Component.literal("Behaviour:"));
            ((IDebuggable) behaviour).getDebugInfo(left, right, side);
//            left.add("");
            left.add(Component.literal(""));
        } else {
//            left.add("Behaviour = " + behaviour.getClass());
            left.add(Component.literal("Behaviour = " + behaviour.getClass()));
        }

        if (flow instanceof IDebuggable) {
//            left.add("Flow:");
            left.add(Component.literal("Flow:"));
            ((IDebuggable) flow).getDebugInfo(left, right, side);
//            left.add("");
            left.add(Component.literal(""));
        } else {
//            left.add("Flow = " + flow.getClass());
            left.add(Component.literal("Flow = " + flow.getClass()));
        }
        for (Direction face : Direction.VALUES) {
//            right.add(face + " = " + types.get(face) + ", " + getConnectedDist(face));
            right.add(Component.literal(face + " = " + types.get(face) + ", " + getConnectedDist(face)));
        }
    }
}
