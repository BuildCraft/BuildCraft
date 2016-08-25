package buildcraft.lib.bpt.helper;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.misc.NBTUtils;

public class BptTaskBlockStandalone extends BptTaskSimple {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib:bpt_block_standalone");
    private final BlockPos pos;
    private final IBlockState state;
    private final IRequestedItem reqItem;
    private boolean hasSent = false;

    public BptTaskBlockStandalone(BlockPos pos, IBlockState state, IBuilderAccessor accessor) {
        this(pos, state, accessor.requestStackForBlock(state));
    }

    public BptTaskBlockStandalone(BlockPos pos, IBlockState state, IRequestedItem item) {
        super(MjAPI.MJ / 2);
        this.pos = pos;
        this.state = state;
        this.reqItem = item;
    }

    public BptTaskBlockStandalone(NBTTagCompound nbt, IBuilderAccessor accessor) {
        super(nbt.getCompoundTag("super"));
        this.pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
        Block block = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("block")));
        this.state = NBTUtils.readBlockStateProperties(block.getDefaultState(), nbt.getCompoundTag("state"));
        this.hasSent = nbt.getBoolean("hasSent");
        if (!hasSent) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
            reqItem = accessor.requestStack(stack);
            reqItem.lock();
        } else {
            reqItem = null;
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("super", super.serializeNBT());
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.setString("block", state.getBlock().getRegistryName().toString());
        nbt.setTag("state", NBTUtils.writeBlockStateProperties(state));
        nbt.setTag("stack", reqItem.getRequested().serializeNBT());
        return nbt;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public Set<EnumFacing> getRequiredSolidFaces(IBuilderAccessor builder) {
        return ImmutableSet.of();
    }

    @Override
    public boolean isDone(IBuilderAccessor builder) {
        return builder.getWorld().getBlockState(pos) == state;
    }

    @Override
    protected void onReceiveFullPower(IBuilderAccessor builder) {
        if (hasSent) {
            return;
        }
        if (reqItem.lock()) {
            int time = builder.startBlockAnimation(new Vec3d(pos), state, 0);
            builder.addAction(new BptActionSetBlockState(state, pos, reqItem), time);
            hasSent = true;
        }
    }
}
