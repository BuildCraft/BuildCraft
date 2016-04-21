package buildcraft.lib.bpt.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBptTaskDeserializer;
import buildcraft.api.bpt.IBuilder;

public class BptTaskBlockClear extends BptTaskSimple {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftapi:bpt_block_clear");
    private final IBuilder builder;
    private final BlockPos pos;
    private final int ticks;

    public static BptTaskBlockClear create(IBuilder builder, BlockPos offset) {
        BlockPos pos = builder.getPos().add(offset);
        World world = builder.getWorld();
        IBlockState state = world.getBlockState(pos);
        int milliJoules = (int) (state.getBlockHardness(world, pos) * 1000);
        int ticks = milliJoules / 200;
        return new BptTaskBlockClear(milliJoules, ticks, builder, pos);
    }

    private BptTaskBlockClear(int milliJoules, int ticks, IBuilder builder, BlockPos pos) {
        super(milliJoules);
        this.builder = builder;
        this.ticks = ticks;
        this.pos = pos;
    }

    public BptTaskBlockClear(NBTTagCompound nbt, IBuilder builder) {
        super(nbt.getCompoundTag("super"));
        this.builder = builder;
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
    public boolean isDone() {
        return builder.getWorld().isAirBlock(pos);
    }

    @Override
    public boolean isReady() {
        return !isDone();
    }

    @Override
    protected void onReceiveFullPower() {
        int[] ticks = builder.startPowerBuilding(new Vec3d(pos), required, 0);
        int start = ticks[0];
        int end = ticks[1];
        for (int i = 0; i < end - start; i++) {
            builder.addAction(new BptActionPartiallyBreakBlock((i + 1) / (float) i, pos), start + i);
        }
        builder.addAction(new BptActionSetBlockState(Blocks.AIR.getDefaultState(), pos), end);
    }

    public enum Deserializer implements IBptTaskDeserializer {
        INSTANCE;

        @Override
        public IBptTask deserializeNBT(NBTTagCompound nbt, IBuilder builder) {
            return new BptTaskBlockClear(nbt, builder);
        }
    }
}
