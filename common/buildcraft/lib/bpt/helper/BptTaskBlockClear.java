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

import buildcraft.api.bpt.IBuilder;

public class BptTaskBlockClear extends BptTaskSimple {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib:bpt_block_clear");
    private final BlockPos pos;
    private final int ticks;

    public static BptTaskBlockClear create(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        int milliJoules = (int) (state.getBlockHardness(world, pos) * 1000);
        int ticks = milliJoules / 200;
        return new BptTaskBlockClear(milliJoules, ticks, pos);
    }

    private BptTaskBlockClear(int milliJoules, int ticks, BlockPos pos) {
        super(milliJoules);
        this.ticks = ticks;
        this.pos = pos;
    }

    public BptTaskBlockClear(NBTTagCompound nbt) {
        super(nbt.getCompoundTag("super"));
        this.ticks = nbt.getInteger("ticks");
        int[] pos = nbt.getIntArray("pos");
        this.pos = new BlockPos(pos[0], pos[1], pos[2]);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("super", super.serializeNBT());
        nbt.setInteger("ticks", ticks);
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        return nbt;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public boolean isDone(IBuilder builder) {
        return builder.getWorld().isAirBlock(pos);
    }

    @Override
    public Set<EnumFacing> getRequiredSolidFaces(IBuilder builder) {
        return ImmutableSet.of();
    }

    @Override
    protected void onReceiveFullPower(IBuilder builder) {
        int[] ticks = builder.startPowerAnimation(new Vec3d(pos), required, 0);
        int start = ticks[0];
        int end = ticks[1];
        for (int i = 0; i < end - start; i++) {
            builder.addAction(new BptActionPartiallyBreakBlock((i + 1) / (float) i, pos), start + i);
        }
        builder.addAction(new BptActionSetBlockState(Blocks.AIR.getDefaultState(), pos), end);
    }
}
