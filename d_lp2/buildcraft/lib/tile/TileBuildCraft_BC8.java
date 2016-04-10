package buildcraft.lib.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;

import buildcraft.lib.data.DataTemplate;

public abstract class TileBuildCraft_BC8 extends TileEntity {
    private final int stageCount;

    public TileBuildCraft_BC8() {
        this(1);
    }

    public TileBuildCraft_BC8(int stageCount) {
        this.stageCount = stageCount;
    }

    /** Checks to see if this tile can update. The base implementation only checks to see if it has a world. */
    public boolean cannotUpdate() {
        return !hasWorldObj();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public void redrawBlock() {
        if (hasWorldObj()) worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    @Override
    public final void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = nbt.getTagList("parts", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < stageCount; i++) {
            readFromNBT(i, list.getCompoundTagAt(i));
        }
    }

    @Override
    public final void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < stageCount; i++) {
            list.appendTag(writeToNBT(i));
        }
        nbt.setTag("parts", list);
    }

    public abstract void readFromNBT(int stage, NBTTagCompound nbt);

    public abstract NBTTagCompound writeToNBT(int stage);

    /** Gets the template for packet data, for the specified stage. */
    public abstract DataTemplate getTemplateFor(int stage);
}
