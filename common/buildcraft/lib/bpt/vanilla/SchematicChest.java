package buildcraft.lib.bpt.vanilla;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;

public class SchematicChest extends SchematicBlock {
    private final ItemStack[] stacks = new ItemStack[27];

    public SchematicChest(IBlockState at, TileEntityChest chest) {
        super(at);
        for (int i = 0; i < 27; i++) {
            stacks[i] = chest.getStackInSlot(i);
            if (stacks[i] != null) {
                stacks[i] = stacks[i].copy();
            }
        }
    }

    public SchematicChest(NBTTagCompound nbt) throws SchematicException {
        super(nbt);
        NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < 27 && i < list.tagCount(); i++) {
            stacks[i] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < 27; i++) {
            ItemStack stack = stacks[i];
            if (stack == null) {
                list.appendTag(new NBTTagCompound());
            } else {
                list.appendTag(stack.serializeNBT());
            }
        }
        nbt.setTag("items", list);
        return nbt;
    }

    @Override
    public Iterable<IBptTask> createTasks(IBuilderAccessor builder, BlockPos pos) {
        if (canEdit(builder, pos)) {
            return ImmutableList.of(new BptTaskPlaceAndFillChest(pos, state, stacks, builder));
        } else {
            return ImmutableList.of();
        }
    }

    @Override
    public PreBuildAction createClearingTask(IBuilderAccessor builder, BlockPos pos) {
        return DefaultBptActions.REQUIRE_AIR;
    }
}
