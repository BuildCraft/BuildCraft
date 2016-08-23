package buildcraft.lib.bpt.helper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptAction;
import buildcraft.api.bpt.IBptReader;
import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;
import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.SoundUtil;

public class BptActionSetBlockState implements IBptAction {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib", "set_block_state");
    private final IBlockState state;
    private final BlockPos pos;
    private final IRequestedItem reqItem;

    public BptActionSetBlockState(IBlockState state, BlockPos pos, IRequestedItem reqItem) {
        this.state = state;
        this.pos = pos;
        this.reqItem = reqItem;
    }

    public BptActionSetBlockState(NBTTagCompound nbt, IBuilderAccessor accessor) {
        String regName = nbt.getString("block");
        Block block = Block.REGISTRY.getObject(new ResourceLocation(regName));
        this.state = NBTUtils.readBlockStateProperties(block.getDefaultState(), nbt.getCompoundTag("state"));
        this.pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
        ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
        reqItem = accessor.requestStack(stack);
        reqItem.lock();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("block", state.getBlock().getRegistryName().toString());
        nbt.setTag("state", NBTUtils.writeBlockStateProperties(state));
        nbt.setTag("pos", NBTUtils.writeBlockPos(pos));
        nbt.setTag("stack", reqItem.getRequested().serializeNBT());
        return nbt;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public void run(IBuilderAccessor builder) {
        if (reqItem.lock()) {// Just make sure...
            if (state.getBlock() == Blocks.AIR) {
                SoundUtil.playBlockBreak(builder.getWorld(), pos, builder.getWorld().getBlockState(pos));
                builder.getWorld().setBlockToAir(pos);
            } else {
                builder.getWorld().setBlockState(pos, state);
                SoundUtil.playBlockPlace(builder.getWorld(), pos, state);
            }
            reqItem.use();
        } else {
            BCLog.logger.warn("[lib.bpt.action] Failed to aquire a lock on " + reqItem + "!");
        }
    }

    public enum Deserializer implements IBptReader<IBptAction> {
        INSTANCE;

        @Override
        public IBptAction deserialize(NBTTagCompound nbt, IBuilderAccessor accessor) {
            return new BptActionSetBlockState(nbt, accessor);
        }
    }
}
