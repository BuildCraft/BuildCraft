package buildcraft.lib.bpt.vanilla;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IMaterialProvider;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.bpt.task.TaskBuilder;
import buildcraft.lib.bpt.task.TaskBuilder.PostTask;
import buildcraft.lib.bpt.task.TaskBuilder.RequirementBuilder;
import buildcraft.lib.bpt.task.TaskUsable;
import buildcraft.lib.misc.InventoryUtil;
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
    public TaskUsable createTask(IBuilderAccessor builder, BlockPos pos) {
        TaskBuilder t = new TaskBuilder();
        IRequestedItem stateReq = t.request("state", state);
        IRequestedItem[] stackReq = new IRequestedItem[27];
        for (int i = 0; i < 27; i++) {
            if (stacks[i] != null) {
                stackReq[i] = t.request("stack[" + i + "]", stacks[i]);
            }
        }

        PostTask placeChest = t.doWhen(t.requirement().lock(stateReq).power(MjAPI.MJ * 2), (b, p) -> {
            stateReq.use();
            b.getWorld().setBlockState(p, state);
            SoundUtil.playBlockPlace(b.getWorld(), p);
            TileEntityChest tileChest = (TileEntityChest) b.getWorld().getTileEntity(p);
            tileChest.numPlayersUsing++;
        });
        PostTask[] post = new PostTask[27];
        RequirementBuilder reqClose = t.requirement();
        reqClose.after(placeChest, true);

        for (int i = 0; i < 27; i++) {
            if (stacks[i] != null) {
                final int index = i;
                post[index] = t.doWhen(t.requirement().lock(stackReq[index]).power(MjAPI.MJ).after(placeChest, true), (b, p) -> {
                    TileEntity tile = b.getWorld().getTileEntity(p);
                    if (tile instanceof TileEntityChest) {
                        TileEntityChest chest = (TileEntityChest) tile;
                        ItemStack existing = chest.getStackInSlot(index);
                        if (existing == null) {
                            stackReq[index].use();
                            chest.setInventorySlotContents(index, stacks[index].copy());
                        } else if (ItemStack.areItemStacksEqual(existing, stacks[index])) {
                            stackReq[index].release();
                        } else {
                            InventoryUtil.drop(b.getWorld(), p, existing);
                            stackReq[index].use();
                            chest.setInventorySlotContents(index, stacks[index].copy());
                        }
                    }
                });
                reqClose.after(post[index], false);
            }
        }

        t.doWhen(reqClose, (b, p) -> {
            TileEntity tile = b.getWorld().getTileEntity(p);
            if (tile instanceof TileEntityChest) {
                ((TileEntityChest) tile).numPlayersUsing--;
            }
        });

        return t.build().createUsableTask();
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
    public int getTimeCost() {
        return 80;
    }

    @Override
    public PreBuildAction createClearingTask(IBuilderAccessor builder, BlockPos pos) {
        return DefaultBptActions.REQUIRE_AIR;
    }
}
