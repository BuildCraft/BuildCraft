package buildcraft.lib.bpt.helper;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.lib.bpt.builder.RequestedFree.FreeItem;
import buildcraft.lib.misc.NBTUtils;

public class BptTaskBlockClear extends BptTaskSimple {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib:bpt_block_clear");
    private final BlockPos pos;
    private final int ticks;
    private boolean hasSent = false;

    public static BptTaskBlockClear create(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        // TODO: replace this with BlockUtils.computeBlockBreakPower after it has been converted to long MJ
        long microJoules = (long) (state.getBlockHardness(world, pos) * 1000);
        int ticks = (int) (microJoules / 200_000);
        return new BptTaskBlockClear(microJoules, ticks, pos);
    }

    private BptTaskBlockClear(long microJoules, int ticks, BlockPos pos) {
        super(microJoules);
        this.ticks = ticks;
        this.pos = pos;
    }

    public BptTaskBlockClear(NBTTagCompound nbt) {
        super(nbt.getCompoundTag("super"));
        this.ticks = nbt.getInteger("ticks");
        this.pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
        this.hasSent = nbt.getBoolean("hasSent");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("super", super.serializeNBT());
        nbt.setInteger("ticks", ticks);
        nbt.setTag("pos", NBTUtils.writeBlockPos(pos));
        nbt.setBoolean("hasSent", hasSent);
        return nbt;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public boolean isDone(IBuilderAccessor builder) {
        return builder.getWorld().isAirBlock(pos);
    }

    @Override
    public Set<EnumFacing> getRequiredSolidFaces(IBuilderAccessor builder) {
        return ImmutableSet.of();
    }

    @Override
    protected void onReceiveFullPower(IBuilderAccessor builder) {
        if (!hasSent) {
            int[] anim = builder.startPowerAnimation(new Vec3d(pos), required, 0);
            int start = anim[0];
            int end = anim[1];
            for (int i = start; i < end; i++) {
                int j = i - start;
                builder.addAction(new BptActionPartiallyBreakBlock((j + 1) / (float) j, pos), i);
            }
            builder.addAction(new BptActionSetBlockState(Blocks.AIR.getDefaultState(), pos, FreeItem.NO_ITEM), end);
            hasSent = true;
        }
    }
}
