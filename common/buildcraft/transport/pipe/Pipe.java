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

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.data.LoadingException;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.PipeBehaviour;
import buildcraft.transport.api_move.PipeDefinition;
import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.pipes.events.PipeEvent;

public final class Pipe implements IPipe {
    public static final int NET_RENDER = 0;

    private final IPipeHolder holder;
    private final PipeDefinition definition;
    private final PipeBehaviour behaviour;
    private EnumDyeColor colour = null;
    private boolean updateMarked = true;
    private final EnumSet<EnumFacing> connected = EnumSet.noneOf(EnumFacing.class);
    private final EnumMap<EnumFacing, Integer> textures = new EnumMap<>(EnumFacing.class);
    private final EnumMap<EnumFacing, ConnectedType> types = new EnumMap<>(EnumFacing.class);

    public Pipe(IPipeHolder holder, PipeBehaviour behaviour) {
        this.holder = holder;
        this.definition = behaviour.getDefinition();
        this.behaviour = behaviour;
    }

    // read + write

    public Pipe(IPipeHolder holder, NBTTagCompound nbt) throws LoadingException {
        this.holder = holder;
        this.definition = PipeDefinition.loadDefinition(nbt.getString("def"));
        this.behaviour = definition.logicLoader.loadBehaviour(this, nbt.getCompoundTag("beh"));
        this.colour = NBTUtils.readEnum(nbt.getTag("col"), EnumDyeColor.class);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("def", definition.key.toString());
        nbt.setTag("beh", behaviour.writeToNbt());
        nbt.setTag("col", NBTUtils.writeEnum(colour));
        return nbt;
    }

    // network

    public Pipe(IPipeHolder holder, PacketBuffer buffer) throws IOException {
        this.holder = holder;
        try {
            this.definition = PipeDefinition.loadDefinition(buffer.readStringFromBuffer(256));
        } catch (LoadingException e) {
            throw new IOException(e);
        }
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        this.colour = NetworkUtils.readEnum(buffer, EnumDyeColor.class);
    }

    public void writeCreationPayload(PacketBuffer buffer) {
        buffer.writeString(definition.key.toString());
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
    public EnumDyeColor getColour() {
        return this.colour;
    }

    @Override
    public void setColour(EnumDyeColor colour) {
        this.colour = colour;
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
                    EnumDyeColor oColour = behaviour.pipe.getColour();
                    if (colour != null && oColour != null && colour != oColour) {
                        continue;
                    }
                    if (behaviour.canConnect(facing, oBehaviour) && oBehaviour.canConnect(facing.getOpposite(), behaviour)) {
                        connected.add(facing);
                        types.put(facing, ConnectedType.PIPE);
                    }
                } else if (oTile != null) {
                    // TODO: custom pipe connections! (custom tiles basically)
                    if (behaviour.canConnect(facing, oTile)) {
                        connected.add(facing);
                        types.put(facing, ConnectedType.TILE);
                    }
                }
                if (connected.contains(facing)) {
                    textures.put(facing, behaviour.getTextureIndex(facing));
                }
            }
            getHolder().scheduleUpdatePacket();
        }
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

    public ConnectedType getConnectedType(EnumFacing side) {
        return types.get(side);
    }
}
