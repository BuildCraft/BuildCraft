package buildcraft.transport.pipe;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.LoadingException;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.client.model.key.PipeModelKey;

public final class Pipe implements IPipe, IDebuggable {
    public static final int NET_RENDER = 0;

    public final IPipeHolder holder;
    public final PipeDefinition definition;
    public final PipeBehaviour behaviour;
    public final PipeFlow flow;
    private EnumDyeColor colour = null;
    private boolean updateMarked = true;
    private final EnumMap<EnumFacing, Float> connected = new EnumMap<>(EnumFacing.class);
    private final EnumMap<EnumFacing, Integer> textures = new EnumMap<>(EnumFacing.class);
    private final EnumMap<EnumFacing, ConnectedType> types = new EnumMap<>(EnumFacing.class);

    @SideOnly(Side.CLIENT)
    private PipeModelKey lastModel;

    public Pipe(IPipeHolder holder, PipeDefinition definition) {
        this.holder = holder;
        this.definition = definition;
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        this.flow = definition.flowType.creator.createFlow(this);
    }

    // read + write

    public Pipe(IPipeHolder holder, NBTTagCompound nbt) throws LoadingException {
        this.holder = holder;
        this.colour = NBTUtilBC.readEnum(nbt.getTag("col"), EnumDyeColor.class);
        this.definition = PipeRegistry.INSTANCE.loadDefinition(nbt.getString("def"));
        this.behaviour = definition.logicLoader.loadBehaviour(this, nbt.getCompoundTag("beh"));
        this.flow = definition.flowType.loader.loadFlow(this, nbt.getCompoundTag("flow"));
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("col", NBTUtilBC.writeEnum(colour));
        nbt.setString("def", definition.identifier.toString());
        nbt.setTag("beh", behaviour.writeToNbt());
        nbt.setTag("flow", flow.writeToNbt());
        return nbt;
    }

    // network

    public Pipe(IPipeHolder holder, PacketBufferBC buffer, MessageContext ctx) throws IOException {
        this.holder = holder;
        try {
            this.definition = PipeRegistry.INSTANCE.loadDefinition(buffer.readString(256));
        } catch (LoadingException e) {
            throw new IOException(e);
        }
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        readPayload(buffer, Side.CLIENT, ctx);
        this.flow = definition.flowType.creator.createFlow(this);
        this.flow.readPayload(PipeFlow.NET_ID_FULL_STATE, buffer, Side.CLIENT);
    }

    public void writeCreationPayload(PacketBufferBC buffer) {
        buffer.writeString(definition.identifier.toString());
        writePayload(buffer, Side.SERVER);
        flow.writePayload(PipeFlow.NET_ID_FULL_STATE, buffer, Side.SERVER);
    }

    public void writePayload(PacketBufferBC buffer, Side side) {
        if (side == Side.SERVER) {
            buffer.writeByte(colour == null ? 0 : colour.getMetadata() + 1);
            for (EnumFacing face : EnumFacing.VALUES) {
                Float con = connected.get(face);
                if (con != null && textures.get(face) != null) {
                    buffer.writeBoolean(true);
                    buffer.writeFloat(con.floatValue());

                    Integer tex = textures.get(face);
                    buffer.writeByte(tex.intValue());
                    MessageUtil.writeEnumOrNull(buffer, types.get(face));
                } else {
                    buffer.writeBoolean(false);
                }
            }
            behaviour.writePayload(buffer, side);
        }
    }

    @SideOnly(Side.CLIENT)
    public void readPayload(PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        if (side == Side.CLIENT) {
            connected.clear();
            textures.clear();
            types.clear();

            int nColour = buffer.readUnsignedByte();
            colour = nColour == 0 ? null : EnumDyeColor.byMetadata(nColour - 1);

            for (EnumFacing face : EnumFacing.VALUES) {
                if (buffer.readBoolean()) {
                    float dist = buffer.readFloat();
                    int tex = buffer.readUnsignedByte();

                    connected.put(face, dist);
                    textures.put(face, Integer.valueOf(tex));

                    ConnectedType type = MessageUtil.readEnumOrNull(buffer, ConnectedType.class);
                    types.put(face, type);
                }
            }

            behaviour.readPayload(buffer, side, ctx);

            PipeModelKey model = getModel();
            if (!model.equals(lastModel)) {
                lastModel = model;
                getHolder().scheduleRenderUpdate();
            }
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
    public EnumDyeColor getColour() {
        return this.colour;
    }

    @Override
    public void setColour(EnumDyeColor colour) {
        this.colour = colour;
        markForUpdate();
    }

    // Caps

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (behaviour.hasCapability(capability, facing)) return true;
        return flow.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        T val = behaviour.getCapability(capability, facing);
        if (val != null) return val;
        return flow.getCapability(capability, facing);
    }

    // misc

    public void onLoad() {
        updateConnections();
    }

    public void onTick() {
        behaviour.onTick();
        flow.onTick();
        if (updateMarked) {
            updateConnections();
        }
    }

    private void updateConnections() {
        if (holder.getPipeWorld().isRemote) {
            return;
        }
        updateMarked = false;

        EnumMap<EnumFacing, Float> old = connected.clone();

        connected.clear();
        types.clear();
        textures.clear();

        for (EnumFacing facing : EnumFacing.VALUES) {
            PipePluggable plug = getHolder().getPluggable(facing);
            if (plug != null && plug.isBlocking()) {
                continue;
            }
            TileEntity oTile = getHolder().getNeighbourTile(facing);
            if (oTile == null) {
                continue;
            }
            IPipe oPipe = getHolder().getNeighbourPipe(facing);
            if (oPipe != null) {
                PipeBehaviour oBehaviour = oPipe.getBehaviour();
                if (oBehaviour == null) {
                    continue;
                }
                PipePluggable oPlug = oTile.getCapability(PipeApi.CAP_PLUG, facing.getOpposite());
                if (oPlug != null && oPlug.isBlocking()) {
                    continue;
                }
                if (canPipesConnect(facing, this, oPipe)) {
                    connected.put(facing, 0.25f);
                    types.put(facing, ConnectedType.PIPE);
                }
            } else {
                BlockPos nPos = holder.getPipePos().offset(facing);
                IBlockState neighbour = holder.getPipeWorld().getBlockState(nPos);

                ICustomPipeConnection cust = PipeConnectionAPI.getCustomConnection(neighbour.getBlock());
                if (cust == null) {
                    cust = DefaultPipeConnection.INSTANCE;
                }
                float ext = 0.25f + cust.getExtension(holder.getPipeWorld(), nPos, facing.getOpposite(), neighbour);

                if (behaviour.canConnect(facing, oTile) & flow.canConnect(facing, oTile)) {
                    connected.put(facing, ext);
                    types.put(facing, ConnectedType.TILE);
                }
            }
            if (connected.containsKey(facing)) {
                textures.put(facing, behaviour.getTextureIndex(facing));
            }
        }
        if (!old.equals(connected)) {
            for (EnumFacing face : EnumFacing.VALUES) {
                boolean o = old.containsKey(face);
                boolean n = connected.containsKey(face);
                if (o != n) {
                    IPipe oPipe = getHolder().getNeighbourPipe(face);
                    if (oPipe != null) {
                        oPipe.markForUpdate();
                    }
                }
            }
        }
        getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
    }

    public void getDrops(NonNullList<ItemStack> toDrop) {
        Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(definition);
        if (item != null) {
            toDrop.add(new ItemStack(item, 1, colour == null ? 0 : 1 + colour.ordinal()));
        }
    }

    public static boolean canPipesConnect(EnumFacing to, IPipe one, IPipe two) {
        return canColoursConnect(one.getColour(), two.getColour())//
            && canBehavioursConnect(to, one.getBehaviour(), two.getBehaviour())//
            && canFlowsConnect(to, one.getFlow(), two.getFlow());
    }

    public static boolean canColoursConnect(EnumDyeColor one, EnumDyeColor two) {
        return one == null ? true : (two == null ? true : one == two);
    }

    public static boolean canBehavioursConnect(EnumFacing to, PipeBehaviour one, PipeBehaviour two) {
        return one.canConnect(to, two) && two.canConnect(to.getOpposite(), one);
    }

    public static boolean canFlowsConnect(EnumFacing to, PipeFlow one, PipeFlow two) {
        return one.canConnect(to, two) && two.canConnect(to.getOpposite(), one);
    }

    @Override
    public void markForUpdate() {
        updateMarked = true;
    }

    @SideOnly(Side.CLIENT)
    public PipeModelKey getModel() {
        int[] sides = new int[6];
        float[] mc = new float[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            int i = face.ordinal();
            sides[i] = behaviour.getTextureIndex(face);
            mc[i] = getConnectedDist(face);
        }
        return new PipeModelKey(definition, behaviour.getTextureIndex(null), sides, mc, colour);
    }

    @Override
    public TileEntity getConnectedTile(EnumFacing side) {
        if (connected.containsKey(side)) {
            TileEntity offset = getHolder().getNeighbourTile(side);
            if (offset == null && !getHolder().getPipeWorld().isRemote) {
                markForUpdate();
            } else {
                return offset;
            }
        }
        return null;
    }

    @Override
    public IPipe getConnectedPipe(EnumFacing side) {
        if (connected.containsKey(side) && getConnectedType(side) == ConnectedType.PIPE) {
            IPipe offset = getHolder().getNeighbourPipe(side);
            if (offset == null && !getHolder().getPipeWorld().isRemote) {
                markForUpdate();
            } else {
                return offset;
            }
        }
        return null;
    }

    @Override
    public ConnectedType getConnectedType(EnumFacing side) {
        return types.get(side);
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        return connected.containsKey(side);
    }

    public float getConnectedDist(EnumFacing face) {
        Float custom = connected.get(face);
        return custom == null ? 0 : custom.floatValue();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("Colour = " + colour);
        left.add("Definition = " + definition.identifier);
        if (behaviour instanceof IDebuggable) {
            left.add("Behaviour:");
            ((IDebuggable) behaviour).getDebugInfo(left, right, side);
            left.add("");
        } else {
            left.add("Behaviour = " + behaviour.getClass());
        }

        if (flow instanceof IDebuggable) {
            left.add("Flow:");
            ((IDebuggable) flow).getDebugInfo(left, right, side);
            left.add("");
        } else {
            left.add("Flow = " + flow.getClass());
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            right.add(face + " = " + types.get(face) + ", " + getConnectedDist(face));
        }
    }
}
