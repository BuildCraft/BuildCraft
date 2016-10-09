package buildcraft.transport.pipe;

import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.data.LoadingException;
import buildcraft.transport.api_move.*;
import buildcraft.transport.api_move.IPipeHolder.PipeMessageReceiver;
import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.pipes.events.PipeEvent;

public final class Pipe implements IPipe, IDebuggable {
    public static final int NET_RENDER = 0;

    public final IPipeHolder holder;
    public final PipeDefinition definition;
    public final PipeBehaviour behaviour;
    public final PipeFlow flow;
    private EnumDyeColor colour = null;
    private boolean updateMarked = true;
    private final EnumSet<EnumFacing> connected = EnumSet.noneOf(EnumFacing.class);
    private final EnumMap<EnumFacing, Integer> textures = new EnumMap<>(EnumFacing.class);
    private final EnumMap<EnumFacing, ConnectedType> types = new EnumMap<>(EnumFacing.class);

    public Pipe(IPipeHolder holder, PipeDefinition definition) {
        this.holder = holder;
        this.definition = definition;
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        this.flow = definition.flowType.creator.createFlow(this);
        behaviour.configureFlow(flow);
    }

    // read + write

    public Pipe(IPipeHolder holder, NBTTagCompound nbt) throws LoadingException {
        this.holder = holder;
        this.colour = NBTUtils.readEnum(nbt.getTag("col"), EnumDyeColor.class);
        this.definition = PipeRegistry.INSTANCE.loadDefinition(nbt.getString("def"));
        this.behaviour = definition.logicLoader.loadBehaviour(this, nbt.getCompoundTag("beh"));
        this.flow = definition.flowType.loader.loadFlow(this, nbt.getCompoundTag("flow"));
        behaviour.configureFlow(flow);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("col", NBTUtils.writeEnum(colour));
        nbt.setString("def", definition.identifier.toString());
        nbt.setTag("beh", behaviour.writeToNbt());
        nbt.setTag("flow", flow.writeToNbt());
        return nbt;
    }

    // network

    public Pipe(IPipeHolder holder, PacketBuffer buffer, MessageContext ctx) throws IOException {
        this.holder = holder;
        try {
            this.definition = PipeRegistry.INSTANCE.loadDefinition(buffer.readStringFromBuffer(256));
        } catch (LoadingException e) {
            throw new IOException(e);
        }
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        readPayload(buffer, Side.CLIENT, ctx);
        this.flow = definition.flowType.creator.createFlow(this);
        this.flow.readPayload(PipeFlow.NET_ID_FULL_STATE, buffer, Side.CLIENT);
    }

    public void writeCreationPayload(PacketBuffer buffer) {
        buffer.writeString(definition.identifier.toString());
        writePayload(buffer, Side.SERVER);
        flow.writePayload(PipeFlow.NET_ID_FULL_STATE, buffer, Side.SERVER);
    }

    public void writePayload(PacketBuffer buffer, Side side) {
        if (side == Side.SERVER) {
            NetworkUtils.writeEnum(buffer, colour);
            for (EnumFacing face : EnumFacing.VALUES) {
                if (connected.contains(face) && textures.get(face) != null) {
                    buffer.writeBoolean(true);

                    Integer tex = textures.get(face);
                    buffer.writeByte(tex.intValue());

                    ConnectedType type = types.get(face);
                    buffer.writeByte(type == null ? 0 : (type == ConnectedType.TILE ? 1 : 2));
                } else {
                    buffer.writeBoolean(false);
                }
            }

            behaviour.writePayload(buffer, side);
        }
    }

    @SideOnly(Side.CLIENT)
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        if (side == Side.CLIENT) {
            PipeModelKey before = getModel();

            connected.clear();
            textures.clear();
            types.clear();

            this.colour = NetworkUtils.readEnum(buffer, EnumDyeColor.class);

            for (EnumFacing face : EnumFacing.VALUES) {
                if (buffer.readBoolean()) {
                    int tex = buffer.readUnsignedByte();
                    int type = buffer.readUnsignedByte();

                    connected.add(face);
                    textures.put(face, Integer.valueOf(tex));
                    if (type != 0) {
                        types.put(face, type == 1 ? ConnectedType.TILE : ConnectedType.PIPE);
                    }
                }
            }

            behaviour.readPayload(buffer, side, ctx);

            if (!before.equals(getModel())) {
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

    public void onTick() {
        behaviour.onTick();
        flow.onTick();
        if (updateMarked) {
            updateMarked = false;

            Set<EnumFacing> old = EnumSet.copyOf(connected);

            connected.clear();
            types.clear();
            textures.clear();

            for (EnumFacing facing : EnumFacing.VALUES) {
                PipePluggable plug = getHolder().getPluggable(facing);
                if (plug != null && plug.isBlocking()) {
                    continue;
                }
                TileEntity oTile = getHolder().getNeighbouringTile(facing);
                IPipe oPipe = getHolder().getNeighbouringPipe(facing);
                if (oPipe != null) {
                    PipeBehaviour oBehaviour = oPipe.getBehaviour();
                    if (oBehaviour == null) {
                        continue;
                    }
                    PipePluggable oPlug = oPipe.getHolder().getPluggable(facing.getOpposite());
                    if (oPlug != null && oPlug.isBlocking()) {
                        continue;
                    }
                    if (canPipesConnect(facing, this, oPipe)) {
                        connected.add(facing);
                        types.put(facing, ConnectedType.PIPE);
                    }
                } else if (oTile != null) {
                    // TODO: custom pipe connections! (custom tiles basically)
                    if (behaviour.canConnect(facing, oTile) & flow.canConnect(facing, oTile)) {
                        connected.add(facing);
                        types.put(facing, ConnectedType.TILE);
                    }
                }
                if (connected.contains(facing)) {
                    textures.put(facing, behaviour.getTextureIndex(facing));
                }
            }
            if (!old.equals(connected)) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    boolean o = old.contains(face);
                    boolean n = connected.contains(face);
                    if (o != n) {
                        IPipe oPipe = getHolder().getNeighbouringPipe(face);
                        if (oPipe != null) {
                            oPipe.markForUpdate();
                        }
                    }
                }
            }

            getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    public void onRemove(List<ItemStack> toDrop) {
        Item item = (Item) PipeAPI.pipeRegistry.getItemForPipe(definition);
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

    // TODO: Replace this with a proper listener system (that allows for multiple listeners as well)
    public void onEvent(PipeEvent event) {}

    @SideOnly(Side.CLIENT)
    public PipeModelKey getModel() {
        int[] sides = new int[6];
        float[] mc = new float[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            int i = face.ordinal();
            sides[i] = behaviour.getTextureIndex(face);
            mc[i] = connected.contains(face) ? 0.25f : 0;
        }
        return new PipeModelKey(definition, behaviour.getTextureIndex(null), sides, mc, colour);
    }

    @Override
    public TileEntity getConnectedTile(EnumFacing side) {
        if (connected.contains(side)) {
            TileEntity offset = getHolder().getNeighbouringTile(side);
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
        if (connected.contains(side)) {
            IPipe offset = getHolder().getNeighbouringPipe(side);
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
        return connected.contains(side);
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

    }
}
