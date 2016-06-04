package buildcraft.lib.bpt.helper;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.bpt.IBuilder;
import buildcraft.lib.misc.NBTUtils;

public class BptTaskBlockStandalone extends BptTaskSimple {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib:bpt_block_standalone");
    private final BlockPos pos;
    private final IBlockState state;

    public static BptTaskBlockStandalone create(BlockPos pos, IBlockState state) {
        return new BptTaskBlockStandalone(pos, state);
    }

    public BptTaskBlockStandalone(BlockPos pos, IBlockState state) {
        super(500);
        this.pos = pos;
        this.state = state;
    }

    public BptTaskBlockStandalone(NBTTagCompound nbt) {
        super(nbt.getCompoundTag("super"));
        int[] pos = nbt.getIntArray("pos");
        this.pos = new BlockPos(pos[0], pos[1], pos[2]);
        Block block = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("block")));
        IBlockState state = block.getDefaultState();
        this.state = NBTUtils.readBlockStateProperties(state, nbt.getCompoundTag("state"));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("super", super.serializeNBT());
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.setString("block", state.getBlock().getRegistryName().toString());
        nbt.setTag("state", NBTUtils.writeBlockStateProperties(state));
        return nbt;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public Set<EnumFacing> getRequiredSolidFaces(IBuilder builder) {
        return ImmutableSet.of();
    }

    @Override
    public boolean isDone(IBuilder builder) {
        return builder.getWorld().getBlockState(pos) == state;
    }

    @Override
    protected void onReceiveFullPower(IBuilder builder) {
        int time = builder.startBlockAnimation(new Vec3d(pos), state, 0);
        builder.addAction(new BptActionSetBlockState(state, pos), time);
    }
}
