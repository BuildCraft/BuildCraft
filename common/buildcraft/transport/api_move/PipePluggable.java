package buildcraft.transport.api_move;

import java.io.IOException;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public abstract class PipePluggable {
    public final PluggableDefinition definition;
    public final IPipeHolder holder;
    public final EnumFacing side;

    public PipePluggable(PluggableDefinition definition, IPipeHolder holder, EnumFacing side) {
        this.definition = definition;
        this.holder = holder;
        this.side = side;
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        return nbt;
    }

    /** Writes the payload that will be passed into
     * {@link PluggableDefinition#loadFromBuffer(IPipeHolder, EnumFacing, PacketBuffer)} on the client. (This is called
     * on the server and sent to the client). Note that this will be called *instead* of write and read payload. */
    public void writeCreationPayload(PacketBuffer buffer) {

    }

    public void writePayload(PacketBuffer buffer, Side side) {

    }

    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {

    }

    public void onTick() {}

    /** @return A bounding box that will be used for collisions and raytracing. */
    public abstract AxisAlignedBB getBoundingBox();

    /** @return True if the pipe cannot connect outwards (it is blocked), or False if this does not block the pipe. */
    public boolean isBlocking() {
        return false;
    }

    public boolean hasCapability(Capability<?> cap) {
        return false;
    }

    public <T> T getCapability(Capability<T> cap) {
        return null;
    }

    /** Called whenever this pluggable is removed from the pipe.
     * 
     * @param toDrop A list containing all the items to drop (so you should add your items to this list) */
    public void onRemove(List<ItemStack> toDrop) {}

    /** Called whenever this pluggable is picked by the player (similar to Block.getPickBlock)
     * 
     * @return The stack that should be picked, or null if no stack can be picked from this pluggable. */
    public ItemStack getPickStack() {
        return null;
    }

    public boolean onPluggableActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public PluggableModelKey<?> getModelRenderKey(BlockRenderLayer layer) {
        return null;
    }
}
