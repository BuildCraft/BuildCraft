package buildcraft.lib.bpt.helper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBptTaskDeserializer;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.bpt.IBuilder.IRequestedStack;

public class BptTaskBlockStandalone extends BptTaskSimple {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftapi:bpt_block_standalone");
    private final IBuilder builder;
    private final BlockPos pos;
    private final IBlockState state;

    public static BptTaskBlockStandalone create(IBuilder builder, BlockPos offset, IBlockState state) {
        return new BptTaskBlockStandalone(builder, offset, state);
    }

    public BptTaskBlockStandalone(IBuilder builder, BlockPos offset, IBlockState state) {
        super(500);
        this.builder = builder;
        this.pos = builder.getPos().add(offset);
        this.state = state;
    }

    public BptTaskBlockStandalone(NBTTagCompound nbt, IBuilder builder) {
        super(nbt.getCompoundTag("super"));
        this.builder = builder;
        int[] pos = nbt.getIntArray("pos");
        this.pos = new BlockPos(pos[0], pos[1], pos[2]);
        Block block = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("block")));
        int meta = nbt.getInteger("meta");
        state = block.getStateFromMeta(meta);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("super", super.serializeNBT());
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.setString("block", state.getBlock().getRegistryName().toString());
        nbt.setInteger("meta", state.getBlock().getMetaFromState(state));
        return nbt;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public boolean isReady() {
        return builder.getWorld().isAirBlock(pos);
    }

    @Override
    public boolean isDone() {
        return builder.getWorld().getBlockState(pos) == state;
    }

    @Override
    protected void onReceiveFullPower() {
        int time = builder.startBlockBuilding(new Vec3d(pos), state, 0);
        builder.addAction(new BptActionSetBlockState(state, pos), time);
    }

    public enum Deserializer implements IBptTaskDeserializer {
        INSTANCE;

        @Override
        public IBptTask deserializeNBT(NBTTagCompound nbt, IBuilder builder) {
            return new BptTaskBlockStandalone(nbt, builder);
        }
    }
}
