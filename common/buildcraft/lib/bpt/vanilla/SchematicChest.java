package buildcraft.lib.bpt.vanilla;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.bpt.*;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;
import buildcraft.lib.misc.SoundUtil;

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
        return ImmutableList.of(new BptTaskPlaceAndFillChest(pos, state, stacks, builder));
    }

    @Override
    public boolean buildImmediatly(World world, IMaterialProvider provider, BlockPos pos) {
        // Try to allocate ALL the requests
        IRequestedItem[] requests = new IRequestedItem[28];
        requests[27] = provider.requestStackForBlock(state);
        if (!requests[27].lock()) {
            requests[27].release();
            return false;
        }
        for (int i = 0; i < 27; i++) {
            if (stacks[i] != null) {
                requests[i] = provider.requestStack(stacks[i]);
                if (!requests[i].lock()) {
                    for (IRequestedItem req : requests) {
                        if (req != null) {
                            req.release();
                        }
                    }
                    return false;
                }
            }
        }
        // Actually build the chest
        world.setBlockState(pos, state);
        SoundUtil.playBlockPlace(world, pos, state);
        TileEntityChest tile = (TileEntityChest) world.getTileEntity(pos);
        if (tile == null) {
            throw new IllegalStateException("Tile entity didn't exist yet! Whaa?");
        }
        for (int i = 0; i < 27; i++) {
            tile.setInventorySlotContents(i, stacks[i]);
        }
        // Use up all of the requests
        for (IRequestedItem req : requests) {
            if (req != null) {
                req.use();
            }
        }
        return true;
    }

    @Override
    public PreBuildAction createClearingTask(IBuilderAccessor builder, BlockPos pos) {
        return DefaultBptActions.REQUIRE_AIR;
    }
}
