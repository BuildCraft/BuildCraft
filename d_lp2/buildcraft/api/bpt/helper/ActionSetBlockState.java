package buildcraft.api.bpt.helper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptAction;
import buildcraft.api.bpt.IBuilder;

public class ActionSetBlockState implements IBptAction {
    public static final ResourceLocation REG_NAME = new ResourceLocation("buildcraftapi", "set_block_state");
    private final IBlockState state;
    private final BlockPos pos;

    public ActionSetBlockState(NBTTagCompound nbt) {
        String regName = nbt.getString("block");
        int meta = nbt.getByte("meta");
        int[] pos = nbt.getIntArray("pos");
        Block block = Block.blockRegistry.getObject(new ResourceLocation(regName));
        this.state = block.getStateFromMeta(meta);
        this.pos = new BlockPos(pos[0], pos[1], pos[2]);
    }

    public ActionSetBlockState(IBlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }

    @Override
    public void run(IBuilder builder) {
        builder.getWorld().setBlockState(pos, state);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("block", state.getBlock().getRegistryName().toString());
        nbt.setByte("meta", (byte) state.getBlock().getMetaFromState(state));
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        return nbt;
    }
}
