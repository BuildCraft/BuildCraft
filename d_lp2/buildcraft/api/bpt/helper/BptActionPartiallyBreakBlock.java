package buildcraft.api.bpt.helper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.IUniqueReader;
import buildcraft.api.bpt.IBptAction;
import buildcraft.api.bpt.IBuilder;

public class BptActionPartiallyBreakBlock implements IBptAction {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftapi", "partial_break_block");
    private final int breakProgress;
    private final BlockPos pos;

    public BptActionPartiallyBreakBlock(float breakProgress, BlockPos pos) {
        this.breakProgress = (int) (breakProgress * 10);
        this.pos = pos;
    }

    public BptActionPartiallyBreakBlock(NBTTagCompound nbt) {
        breakProgress = nbt.getInteger("breakProgress");
        int[] pos = nbt.getIntArray("pos");
        this.pos = new BlockPos(pos[0], pos[1], pos[2]);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("breakProgress", breakProgress);
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        return nbt;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public void run(IBuilder builder) {
        builder.getWorld().sendBlockBreakProgress(pos.hashCode(), pos, breakProgress);
    }

    public enum Deserializer implements IUniqueReader<IBptAction> {
        INSTANCE;

        @Override
        public IBptAction deserialize(NBTTagCompound nbt) {
            return new BptActionPartiallyBreakBlock(nbt);
        }
    }
}
