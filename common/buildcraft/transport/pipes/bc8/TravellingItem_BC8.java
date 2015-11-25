package buildcraft.transport.pipes.bc8;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;

public class TravellingItem_BC8 {
    private final int id;
    private final IPipeContentsEditableItem item;
    private final IPipe_BC8 pipe;
    /** Indicates the in-world tick of when it will reach its destination (Generally the other side of the pipe) */
    private long tickStarted, tickFinished;

    public TravellingItem_BC8(NBTTagCompound nbt, IPipe_BC8 pipe) {
        this.id = nbt.getInteger("id");
        this.item = new PipeContentsEditableItem(nbt.getCompoundTag("item"));
        this.pipe = pipe;
        this.tickStarted = nbt.getLong("tickStarted");
        this.tickFinished = nbt.getLong("tickFinished");
    }

    public TravellingItem_BC8(int id, IPipeContentsEditableItem item, IPipe_BC8 pipe, long now, long reachDest) {
        this.id = id;
        this.item = item;
        this.pipe = pipe;
        this.tickStarted = now;
        this.tickFinished = reachDest;
    }

    public Vec3 interpolatePosition(Vec3 start, Vec3 end, long tick, float partialTicks) {
        long diff = tickFinished - tickStarted;
        long nowDiff = tick - tickStarted;
    }
}
