package buildcraft.lib.bpt.helper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptAction;
import buildcraft.api.bpt.IBptReader;
import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.lib.misc.NBTUtils;

public class BptActionPartiallyBreakBlock implements IBptAction {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib", "partial_break_block");
    private final int breakProgress;
    private final BlockPos pos;

    public BptActionPartiallyBreakBlock(float breakProgress, BlockPos pos) {
        this.breakProgress = (int) (breakProgress * 10);
        this.pos = pos;
    }

    public BptActionPartiallyBreakBlock(NBTTagCompound nbt) {
        breakProgress = nbt.getInteger("breakProgress");
        this.pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("breakProgress", breakProgress);
        nbt.setTag("pos", NBTUtils.writeBlockPos(pos));
        return nbt;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public void run(IBuilderAccessor builder) {
        builder.getWorld().sendBlockBreakProgress(pos.hashCode(), pos, breakProgress);
    }

    public enum Deserializer implements IBptReader<IBptAction> {
        INSTANCE;

        @Override
        public IBptAction deserialize(NBTTagCompound nbt, IBuilderAccessor accessor) {
            return new BptActionPartiallyBreakBlock(nbt);
        }
    }
}
