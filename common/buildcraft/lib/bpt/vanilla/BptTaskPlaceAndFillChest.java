package buildcraft.lib.bpt.vanilla;

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IBuilderAccessor.IRequestedItem;
import buildcraft.lib.bpt.helper.BptActionIItemHandlerSetStack;
import buildcraft.lib.bpt.helper.BptActionSetBlockState;
import buildcraft.lib.bpt.helper.BptTaskSimple;
import buildcraft.lib.misc.NBTUtils;

public class BptTaskPlaceAndFillChest extends BptTaskSimple {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib:bpt_chest_fill");
    private final BlockPos pos;
    private final IBlockState state;
    private final IRequestedItem[] reqItem = new IRequestedItem[28];
    private final boolean[] hasSent = new boolean[29];

    public BptTaskPlaceAndFillChest(BlockPos pos, IBlockState state, ItemStack[] stacks, IBuilderAccessor accessor) {
        super(1000 + costOf(stacks));
        this.pos = pos;
        this.state = state;
        Arrays.fill(hasSent, false);
        reqItem[0] = accessor.requestStackForBlock(state);
        for (int i = 0; i < 27; i++) {
            reqItem[i + 1] = accessor.requestStack(stacks[i]);
        }
    }

    private static int costOf(ItemStack[] stacks) {
        int c = 0;
        for (ItemStack s : stacks) {
            if (s != null) {
                // 1 MJ per full stack
                // * 27 = 27 MJ per full chest
                // but this is in milli MJ so its /1000
                c += (s.stackSize * 100) / 64;
            }
        }
        return c;
    }

    public BptTaskPlaceAndFillChest(NBTTagCompound nbt, IBuilderAccessor accessor) {
        super(nbt);
        this.pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
        Block block = Block.getBlockFromName(nbt.getString("block"));
        if (block instanceof BlockChest) {
            this.state = NBTUtils.readBlockStateProperties(block.getDefaultState(), nbt.getCompoundTag("state"));
            NBTTagCompound subs = nbt.getCompoundTag("requests");
            for (int i = 0; i < 28; i++) {
                boolean has = subs.getBoolean("h" + i);
                hasSent[i] = has;
                if (has) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(subs.getCompoundTag("i" + i));
                    reqItem[i] = accessor.requestStack(stack);
                }
            }
        } else {
            throw new IllegalArgumentException("Unkown chest type " + block);
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setString("block", state.getBlock().getRegistryName().toString());
        nbt.setTag("state", NBTUtils.writeBlockStateProperties(state));
        NBTTagCompound sub = new NBTTagCompound();
        for (int i = 0; i < 28; i++) {
            boolean has = hasSent[i];
            nbt.setBoolean("h" + i, has);
            if (has) {
                ItemStack stack = reqItem[i].getRequested();
                if (stack != null) {
                    sub.setTag("i" + i, stack.serializeNBT());
                }
            }
        }
        nbt.setTag("requests", sub);
        return nbt;
    }

    @Override
    public Set<EnumFacing> getRequiredSolidFaces(IBuilderAccessor builder) {
        return ImmutableSet.of();
    }

    @Override
    public boolean isDone(IBuilderAccessor builder) {
        return hasSent[28];
        //
        // TileEntity tile = builder.getWorld().getTileEntity(pos);
        // if (tile instanceof TileEntityChest) {
        // TileEntityChest chest = (TileEntityChest) tile;
        // for (int i = 0; i < 27; i++) {
        // IRequestedItem requested = reqItem[i + 1];
        // if (requested != null && requested.getRequested() != null) {
        // ItemStack wanted = requested.getRequested();
        // ItemStack existing = chest.getStackInSlot(i);
        // if (!ItemStack.areItemStacksEqual(wanted, existing)) {
        // return false;
        // }
        // }
        // }
        // return true;
        // } else {
        // return false;
        // }
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    protected void onReceiveFullPower(IBuilderAccessor builder) {
        Vec3d to = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (!hasSent[0]) {
            int delay = builder.startBlockAnimation(to, state, 0);
            builder.addAction(new BptActionSetBlockState(state, pos, reqItem[0]), delay);
            // builder.addAction(new BptActionChestOpen(pos), delay + 1);
            hasSent[0] = true;
            return;
        }
        for (int i = 1; i < 28; i++) {
            if (!hasSent[i]) {
                IRequestedItem requested = reqItem[i];
                if (requested == null || requested.getRequested() == null) {
                    hasSent[i] = true;
                    continue;
                }
                if (requested.lock()) {
                    int delay = builder.startItemStackAnimation(to, requested.getRequested(), 0);
                    builder.addAction(new BptActionIItemHandlerSetStack(pos, i, requested), delay);
                    hasSent[i] = true;
                    return;
                }
            }
        }
        if (!hasSent[28]) {
            // builder.addAction(new BptActionChestClose(pos), 1);
            hasSent[28] = true;
        }
    }
}
