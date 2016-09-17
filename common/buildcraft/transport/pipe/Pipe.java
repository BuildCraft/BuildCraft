package buildcraft.transport.pipe;

import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.data.LoadingException;
import buildcraft.transport.api_move.*;
import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.pipes.events.PipeEvent;

public final class Pipe implements IPipe {
    public static final int NET_RENDER = 0;

    private final IPipeHolder holder;
    private final PipeDefinition definition;
    private final PipeBehaviour behaviour;
    private final PipeFlow flow;
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
    }

    // read + write

    public Pipe(IPipeHolder holder, NBTTagCompound nbt) throws LoadingException {
        this.holder = holder;
        this.colour = NBTUtils.readEnum(nbt.getTag("col"), EnumDyeColor.class);
        this.definition = PipeRegistry.INSTANCE.loadDefinition(nbt.getString("def"));
        this.behaviour = definition.logicLoader.loadBehaviour(this, nbt.getCompoundTag("beh"));
        this.flow = definition.flowType.loader.loadFlow(this, nbt.getCompoundTag("flow"));
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

    public Pipe(IPipeHolder holder, PacketBuffer buffer) throws IOException {
        this.holder = holder;
        try {
            this.definition = PipeRegistry.INSTANCE.loadDefinition(buffer.readStringFromBuffer(256));
        } catch (LoadingException e) {
            throw new IOException(e);
        }
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        this.flow = definition.flowType.creator.createFlow(this);
        this.colour = NetworkUtils.readEnum(buffer, EnumDyeColor.class);
    }

    public void writeCreationPayload(PacketBuffer buffer) {
        buffer.writeString(definition.identifier.toString());
        NetworkUtils.writeEnum(buffer, colour);
    }

    public void writePayload(PacketBuffer buffer, Side side) {
        if (side == Side.SERVER) {
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
        }
    }

    @SideOnly(Side.CLIENT)
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        if (side == Side.CLIENT) {
            PipeModelKey before = getModel();

            connected.clear();
            textures.clear();
            types.clear();

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
    }

    // Caps

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        // TODO: Pluggables
        if (behaviour.hasCapability(capability, facing)) return true;
        return flow.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        // TODO: Pluggables
        T val = behaviour.getCapability(capability, facing);
        if (val != null) return val;
        val = flow.getCapability(capability, facing);
        return val;
    }

    // misc

    public void onTick() {
        behaviour.onTick();
        if (updateMarked) {
            updateMarked = false;

            connected.clear();
            types.clear();
            textures.clear();

            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity oTile = getHolder().getNeighbouringTile(facing);
                IPipe oPipe = getHolder().getNeighbouringPipe(facing);
                if (oPipe != null) {
                    PipeBehaviour oBehaviour = oPipe.getBehaviour();
                    if (oBehaviour == null) {
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
            getHolder().scheduleNetworkUpdate();
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

    public void markForUpdate() {
        updateMarked = true;
    }

    // TODO: Replace this with a proper listener system (that allows for multiple listeners as well)
    public void onEvent(PipeEvent event) {}

    @SideOnly(Side.CLIENT)
    public PipeModelKey getModel() {
        TextureAtlasSprite center = getDefinition().getSprite(behaviour.getTextureIndex(null));
        TextureAtlasSprite[] sides = new TextureAtlasSprite[6];
        boolean[] mc = new boolean[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            int i = face.ordinal();
            sides[i] = getDefinition().getSprite(behaviour.getTextureIndex(face));
            mc[i] = connected.contains(face);
        }
        return new PipeModelKey(center, sides, mc);
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
}
