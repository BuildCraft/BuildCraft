package buildcraft.transport.tile;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.PipeEvent;
import buildcraft.api.transport.neptune.*;

import buildcraft.lib.misc.data.LoadingException;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.PipeEventBus;
import buildcraft.transport.pipe.PluggableHolder;
import buildcraft.transport.wire.WireManager;

public class TilePipeHolder extends TileBC_Neptune implements IPipeHolder, ITickable, IDebuggable {
    public static final int NET_UPDATE_MULTI = 10;
    // 11 -> 19 left for future ID's
    public static final int NET_UPDATE_SINGLE_START = 20;
    public static final int NET_UPDATE_PIPE_BEHAVIOUR = getReceiverId(PipeMessageReceiver.BEHAVIOUR);
    public static final int NET_UPDATE_PIPE_FLOW = getReceiverId(PipeMessageReceiver.FLOW);
    public static final int NET_UPDATE_PLUG_DOWN = getReceiverId(PipeMessageReceiver.PLUGGABLE_DOWN);
    public static final int NET_UPDATE_PLUG_UP = getReceiverId(PipeMessageReceiver.PLUGGABLE_UP);
    public static final int NET_UPDATE_PLUG_NORTH = getReceiverId(PipeMessageReceiver.PLUGGABLE_NORTH);
    public static final int NET_UPDATE_PLUG_SOUTH = getReceiverId(PipeMessageReceiver.PLUGGABLE_SOUTH);
    public static final int NET_UPDATE_PLUG_WEST = getReceiverId(PipeMessageReceiver.PLUGGABLE_WEST);
    public static final int NET_UPDATE_PLUG_EAST = getReceiverId(PipeMessageReceiver.PLUGGABLE_EAST);

    public static final int[] NET_UPDATE_PLUGS = {//
        NET_UPDATE_PLUG_DOWN, NET_UPDATE_PLUG_UP,//
        NET_UPDATE_PLUG_NORTH, NET_UPDATE_PLUG_SOUTH,//
        NET_UPDATE_PLUG_WEST, NET_UPDATE_PLUG_EAST,//
    };

    private static int getReceiverId(PipeMessageReceiver type) {
        return NET_UPDATE_SINGLE_START + type.ordinal();
    }

    public final WireManager wireManager = new WireManager(this);
    public final PipeEventBus eventBus = new PipeEventBus();
    private final Map<EnumFacing, PluggableHolder> pluggables = new EnumMap<>(EnumFacing.class);
    private Pipe pipe;
    private boolean scheduleRenderUpdate = true;
    private final Set<PipeMessageReceiver> networkUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private final Set<PipeMessageReceiver> networkGuiUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private final Map<EnumFacing, WeakReference<TileEntity>> neighbourTiles = new EnumMap<>(EnumFacing.class);

    public TilePipeHolder() {
        for (EnumFacing side : EnumFacing.VALUES) {
            pluggables.put(side, new PluggableHolder(this, side));
        }
    }

    // Read + write

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (pipe != null) {
            nbt.setTag("pipe", pipe.writeToNbt());
        }
        NBTTagCompound plugs = new NBTTagCompound();
        for (EnumFacing face : EnumFacing.VALUES) {
            NBTTagCompound plugTag = pluggables.get(face).writeToNbt();
            if (!plugTag.hasNoTags()) {
                plugs.setTag(face.getName(), plugTag);
            }
        }
        if (!plugs.hasNoTags()) {
            nbt.setTag("plugs", plugs);
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("pipe")) {
            try {
                pipe = new Pipe(this, nbt.getCompoundTag("pipe"));
                eventBus.registerHandler(pipe.behaviour);
                eventBus.registerHandler(pipe.flow);
            } catch (LoadingException e) {
                // For now quit immediately so we can debug the cause
                throw new Error(e);
            }
        }
        NBTTagCompound plugs = nbt.getCompoundTag("plugs");
        for (EnumFacing face : EnumFacing.VALUES) {
            pluggables.get(face).readFromNbt(plugs.getCompoundTag(face.getName()));
        }
    }

    // Misc

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        Item item = stack.getItem();
        if (item instanceof IPipeItem) {
            PipeDefinition definition = ((IPipeItem) item).getDefiniton();
            this.pipe = new Pipe(this, definition);
            eventBus.registerHandler(pipe.behaviour);
            eventBus.registerHandler(pipe.flow);
            int meta = stack.getMetadata();
            if (meta > 0 && meta <= 16) {
                pipe.setColour(EnumDyeColor.byMetadata(meta - 1));
            }
        }
        scheduleRenderUpdate();
    }

    public void refreshNeighbours() {
        for (EnumFacing face : EnumFacing.VALUES) {
            WeakReference<TileEntity> current = neighbourTiles.get(face);
            if (current != null) {
                TileEntity tile = current.get();
                if (tile == null || tile.isInvalid()) {
                    neighbourTiles.remove(face);
                } else {
                    continue;
                }
            }
            TileEntity tile = worldObj.getTileEntity(getPos().offset(face));
            if (tile != null) {
                neighbourTiles.put(face, new WeakReference<>(tile));
            }
        }
    }

    // ITickable

    @Override
    public void update() {
        // Tick objects
        if (pipe != null) {
            pipe.onTick();
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            pluggables.get(face).onTick();
        }

        // Send network updates
        if (networkUpdates.size() > 0) {
            // TODO: Multi-update messages! (multiple updates sent in a single message)
            Set<PipeMessageReceiver> parts = EnumSet.copyOf(networkUpdates);
            for (PipeMessageReceiver part : parts) {
                sendNetworkUpdate(getReceiverId(part));
            }
        }
        // No need to send gui updates to specific players if we just sent off messages to all players.
        networkGuiUpdates.removeAll(networkUpdates);
        networkUpdates.clear();

        if (networkGuiUpdates.size() > 0) {
            // TODO: Multi-update messages! (multiple updates sent in a single message)
            Set<PipeMessageReceiver> parts = EnumSet.copyOf(networkGuiUpdates);
            for (PipeMessageReceiver part : parts) {
                sendNetworkGuiUpdate(getReceiverId(part));
            }
        }
        networkGuiUpdates.clear();

        if (scheduleRenderUpdate) {
            scheduleRenderUpdate = false;
            redrawBlock();
        }
    }

    // Network

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writeCreationPayload(buffer);
                }
                for (EnumFacing face : EnumFacing.VALUES) {
                    pluggables.get(face).writeCreationPayload(buffer);
                }
            } else if (id == NET_UPDATE_PIPE_BEHAVIOUR) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writePayload(buffer, side);
                }
            } else if (id == NET_UPDATE_PIPE_FLOW) {
                if (pipe == null || pipe.flow == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.flow.writePayload(PipeFlow.NET_ID_UPDATE, buffer, side);
                }
            } else if (id == NET_UPDATE_PLUG_DOWN) pluggables.get(EnumFacing.DOWN).writePayload(buffer, side);
            else if (id == NET_UPDATE_PLUG_UP) pluggables.get(EnumFacing.UP).writePayload(buffer, side);
            else if (id == NET_UPDATE_PLUG_NORTH) pluggables.get(EnumFacing.NORTH).writePayload(buffer, side);
            else if (id == NET_UPDATE_PLUG_SOUTH) pluggables.get(EnumFacing.SOUTH).writePayload(buffer, side);
            else if (id == NET_UPDATE_PLUG_WEST) pluggables.get(EnumFacing.WEST).writePayload(buffer, side);
            else if (id == NET_UPDATE_PLUG_EAST) pluggables.get(EnumFacing.EAST).writePayload(buffer, side);
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                if (buffer.readBoolean()) {
                    pipe = new Pipe(this, buffer, ctx);
                    eventBus.registerHandler(pipe.behaviour);
                    eventBus.registerHandler(pipe.flow);
                } else if (pipe != null) {
                    eventBus.unregisterHandler(pipe.behaviour);
                    eventBus.unregisterHandler(pipe.flow);
                    pipe = null;
                }
                for (EnumFacing face : EnumFacing.VALUES) {
                    pluggables.get(face).readCreationPayload(buffer);
                }
            } else if (id == NET_UPDATE_MULTI) {
                int total = buffer.readUnsignedByte();
                for (PipeMessageReceiver type : PipeMessageReceiver.VALUES) {
                    if (((total >> type.ordinal()) & 1) == 1) {
                        readPayload(getReceiverId(type), buffer, side, ctx);
                    }
                }
            } else if (id == NET_UPDATE_PIPE_BEHAVIOUR) {
                if (buffer.readBoolean()) {
                    if (pipe == null) {
                        throw new IllegalStateException("Pipe was null when it shouldn't have been!");
                    } else {
                        pipe.readPayload(buffer, side, ctx);
                    }
                }
            } else if (id == NET_UPDATE_PIPE_FLOW) {
                if (buffer.readBoolean()) {
                    if (pipe == null) {
                        throw new IllegalStateException("Pipe was null when it shouldn't have been!");
                    } else {
                        int fId = buffer.readShort();
                        pipe.flow.readPayload(fId, buffer, side);
                    }
                }
            } else if (id == NET_UPDATE_PLUG_DOWN) pluggables.get(EnumFacing.DOWN).readPayload(buffer, side, ctx);
            else if (id == NET_UPDATE_PLUG_UP) pluggables.get(EnumFacing.UP).readPayload(buffer, side, ctx);
            else if (id == NET_UPDATE_PLUG_NORTH) pluggables.get(EnumFacing.NORTH).readPayload(buffer, side, ctx);
            else if (id == NET_UPDATE_PLUG_SOUTH) pluggables.get(EnumFacing.SOUTH).readPayload(buffer, side, ctx);
            else if (id == NET_UPDATE_PLUG_WEST) pluggables.get(EnumFacing.WEST).readPayload(buffer, side, ctx);
            else if (id == NET_UPDATE_PLUG_EAST) pluggables.get(EnumFacing.EAST).readPayload(buffer, side, ctx);
        }
    }

    // IPipeHolder

    @Override
    public World getPipeWorld() {
        return getWorld();
    }

    @Override
    public BlockPos getPipePos() {
        return getPos();
    }

    @Override
    public TileEntity getPipeTile() {
        return this;
    }

    @Override
    public Pipe getPipe() {
        return pipe;
    }

    @Override
    public PipePluggable getPluggable(EnumFacing side) {
        if (side == null) return null;
        return pluggables.get(side).pluggable;
    }

    public PipePluggable replacePluggable(EnumFacing side, PipePluggable with) {
        PluggableHolder holder = pluggables.get(side);
        PipePluggable old = holder.pluggable;
        holder.pluggable = with;

        eventBus.registerHandler(with);
        eventBus.unregisterHandler(old);

        pipe.markForUpdate();
        scheduleNetworkUpdate(PipeMessageReceiver.PLUGGABLES[side.getIndex()]);
        scheduleRenderUpdate();
        return old;
    }

    @Override
    public TileEntity getNeighbouringTile(EnumFacing side) {
        if (side == null) return null;
        return worldObj.getTileEntity(getPos().offset(side));
        // TODO: Make caching work!
        // WeakReference<TileEntity> weakRef = neighbourTiles.get(side);
        // if (weakRef == null) {
        // return null;
        // }
        // TileEntity tile = weakRef.get();
        // if (tile == null) {
        // return null;
        // } else if (tile.isInvalid()) {
        // neighbourTiles.remove(side);
        // return null;
        // }
        // return tile;
    }

    @Override
    public IPipe getNeighbouringPipe(EnumFacing side) {
        TileEntity neighbour = getNeighbouringTile(side);
        // TODO: move this function to allow for compat support!
        if (neighbour instanceof IPipeHolder) {
            return ((IPipeHolder) neighbour).getPipe();
        }
        return null;
    }

    @Override
    public void scheduleRenderUpdate() {
        scheduleRenderUpdate = true;
    }

    @Override
    public void scheduleNetworkUpdate(PipeMessageReceiver... parts) {
        Collections.addAll(networkUpdates, parts);
    }

    @Override
    public void scheduleNetworkGuiUpdate(PipeMessageReceiver... parts) {
        Collections.addAll(networkGuiUpdates, parts);
    }

    @Override
    public void sendMessage(PipeMessageReceiver to, IWriter writer) {
        createAndSendMessage(getReceiverId(to), (buffer) -> writer.write(buffer));
    }

    @Override
    public WireManager getWireManager() {
        return wireManager;
    }

    @Override
    public boolean fireEvent(PipeEvent event) {
        return eventBus.fireEvent(event);
    }

    // Caps

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (facing != null) {
            PipePluggable plug = getPluggable(facing);
            if (plug != null && plug.isBlocking()) {
                return plug.getCapability(capability);
            }
        }
        return pipe == null ? null : pipe.getCapability(capability, facing);
    }

    // Client side stuffs

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        if (pipe == null) {
            left.add("Pipe = null");
        } else {
            left.add("Pipe:");
            pipe.getDebugInfo(left, right, side);
        }
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }
}
