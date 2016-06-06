package buildcraft.lib.bpt.helper;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.IUniqueReader;
import buildcraft.api.bpt.IBptAction;
import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.lib.misc.SoundUtil;

public class BptActionSetBlockState implements IBptAction {
    public static final ResourceLocation ID = new ResourceLocation("buildcraftlib", "set_block_state");
    private final IBlockState state;
    private final BlockPos pos;

    public BptActionSetBlockState(NBTTagCompound nbt) {
        String regName = nbt.getString("block");
        int meta = nbt.getByte("meta");
        int[] pos = nbt.getIntArray("pos");
        Block block = Block.REGISTRY.getObject(new ResourceLocation(regName));
        this.state = block.getStateFromMeta(meta);// FIXME
        this.pos = new BlockPos(pos[0], pos[1], pos[2]);
    }

    public BptActionSetBlockState(IBlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public void run(IBuilderAccessor builder) {
        builder.getWorld().setBlockState(pos, state);
        SoundUtil.playBlockPlace(builder.getWorld(), pos, state);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("block", state.getBlock().getRegistryName().toString());
        nbt.setByte("meta", (byte) state.getBlock().getMetaFromState(state));
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        return nbt;
    }

    public enum Deserializer implements IUniqueReader<IBptAction> {
        INSTANCE;

        @Override
        public IBptAction deserialize(NBTTagCompound nbt) {
            return new BptActionSetBlockState(nbt);
        }
    }
}
