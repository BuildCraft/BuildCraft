package buildcraft.transport.flow;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.PipeFlow;

public class PipeFlowItems extends PipeFlow {
    public PipeFlowItems(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowItems(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowItems;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return ItemTransactorHelper.getTransactor(oTile, face.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }
}
