package buildcraft.transport.api_move;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public abstract class PipeBehaviour {
    public final IPipe pipe;

    public PipeBehaviour(IPipe pipe) {
        this.pipe = pipe;
    }

    public PipeBehaviour(IPipe pipe, NBTTagCompound nbt) {
        this.pipe = pipe;
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();

        return nbt;
    }

    public abstract int getTextureIndex(EnumFacing face);

    public abstract boolean canConnect(EnumFacing face, PipeBehaviour other);

    public abstract boolean canConnect(EnumFacing face, TileEntity oTile);

    public void onTick() {

    }

    public final PipeDefinition getDefinition() {
        return pipe.getDefinition();
    }
}
