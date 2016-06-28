package buildcraft.lib.bpt.vanilla;

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IBuilderAccessor.IRequestedItem;
import buildcraft.lib.bpt.helper.BptTaskSimple;
import buildcraft.lib.misc.NBTUtils;

public class BptTaskPlaceAndFillChest extends BptTaskSimple {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib:bpt_chest_fill");
    private final BlockPos pos;
    private final IBlockState state;
    private final IRequestedItem[] reqItem = new IRequestedItem[28];
    private final boolean[] hasSent = new boolean[28];

    public BptTaskPlaceAndFillChest(BlockPos pos, IBlockState state, ItemStack[] stacks, IBuilderAccessor accessor) {
        super(10);
        this.pos = pos;
        this.state = state;
        Arrays.fill(hasSent, false);
        reqItem[0] = accessor.requestStackForBlock(state);
        for (int i = 0; i < 27; i++) {
            reqItem[i + 1] = accessor.requestStack(stacks[i]);
        }
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
        TileEntity tile = builder.getWorld().getTileEntity(pos);

        throw new AbstractMethodError("Implement this!");

    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    protected void onReceiveFullPower(IBuilderAccessor builder) {
        throw new AbstractMethodError("Implement this!");
    }
}
