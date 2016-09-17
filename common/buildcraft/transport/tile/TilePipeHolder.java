package buildcraft.transport.tile;

import java.io.IOException;

import net.minecraft.entity.EntityLivingBase;
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
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.IPipeItem;
import buildcraft.transport.api_move.PipeDefinition;
import buildcraft.transport.pipe.Pipe;

public class TilePipeHolder extends TileBC_Neptune implements IPipeHolder, ITickable {
    public static final int NET_PIPE_UPDATE = 10;

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
        }
        scheduleRenderUpdate();
    }

    // ITickable

    @Override
    public void update() {
        if (pipe != null) {
            pipe.onTick();
        }
        if (scheduleRenderUpdate) {
            scheduleRenderUpdate = false;
            scheduleNetworkUpdate = false;
            redrawBlock();
        } else if (scheduleNetworkUpdate) {
            scheduleNetworkUpdate = false;
            sendNetworkUpdate(NET_PIPE_UPDATE);
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
                writePayload(NET_PIPE_UPDATE, buffer, side);
            } else if (id == NET_PIPE_UPDATE) {
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

                readPayload(NET_PIPE_UPDATE, buffer, side, ctx);

            } else if (id == NET_PIPE_UPDATE) {
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
        return pipe == null ? false : pipe.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return pipe == null ? null : pipe.getCapability(capability, facing);
    }
}
