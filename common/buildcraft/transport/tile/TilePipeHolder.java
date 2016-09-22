package buildcraft.transport.tile;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.misc.data.LoadingException;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.transport.api_move.*;
import buildcraft.transport.pipe.Pipe;

public class TilePipeHolder extends TileBC_Neptune implements IPipeHolder, ITickable {
    public static final int NET_UPDATE_MULTI = 10;
    public static final int NET_UPDATE_PIPE = 11;
    public static final int NET_UPDATE_PLUG_START = 12;
    // 12 -> 17 are pluggables faces
    public static final int NET_UPDATE_PLUG_END = 17;

    private final Map<EnumFacing, PipePluggable> pluggables = new EnumMap<>(EnumFacing.class);
    private Pipe pipe;
    private boolean scheduleRenderUpdate = true, scheduleNetworkUpdate = true;

    // Read + write

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (pipe != null) {
            nbt.setTag("pipe", pipe.writeToNbt());
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("pipe")) {
            try {
                pipe = new Pipe(this, nbt.getCompoundTag("pipe"));
            } catch (LoadingException e) {
                // For now quit immediately so we can debug the cause
                throw new Error(e);
            }
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
            int meta = stack.getMetadata();
            if (meta > 0 && meta <= 16) {
                pipe.setColour(EnumDyeColor.byMetadata(meta - 1));
            }
        }
        scheduleRenderUpdate();
    }

    // ITickable

    @Override
    public void update() {
        if (pipe != null) {
            pipe.onTick();
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            PipePluggable plug = pluggables.get(face);
            if (plug != null) {
                plug.onTick();
            }
        }
        if (scheduleRenderUpdate) {
            scheduleRenderUpdate = false;
            scheduleNetworkUpdate = false;
            redrawBlock();
        } else if (scheduleNetworkUpdate) {
            scheduleNetworkUpdate = false;
            sendNetworkUpdate(NET_UPDATE_MULTI);
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
                writePayload(NET_UPDATE_MULTI, buffer, side);
            } else if (id == NET_UPDATE_MULTI) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writePayload(buffer, side);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                if (buffer.readBoolean()) {
                    pipe = new Pipe(this, buffer);
                } else {
                    pipe = null;
                }

                readPayload(NET_UPDATE_MULTI, buffer, side, ctx);

            } else if (id == NET_UPDATE_MULTI) {
                if (buffer.readBoolean()) {
                    if (pipe == null) {
                        throw new IllegalStateException("pipe was null when it shoudn't be!");
                    }
                    pipe.readPayload(buffer, side, ctx);
                } else {
                    pipe = null;
                }
            }
        }
    }

    // IPipeHolder

    @Override
    public World getPipeWorld() {
        return getWorld();
    }

    @Override
    public Pipe getPipe() {
        return pipe;
    }

    @Override
    public PipePluggable getPluggable(EnumFacing side) {
        return pluggables.get(side);
    }

    @Override
    public TileEntity getNeighbouringTile(EnumFacing side) {
        return worldObj.getTileEntity(getPos().offset(side));
    }

    @Override
    public IPipe getNeighbouringPipe(EnumFacing side) {
        // TODO: move this function to allow for compat support!
        TileEntity neighbour = getNeighbouringTile(side);
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
    public void scheduleNetworkUpdate() {
        scheduleNetworkUpdate = true;
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
}
