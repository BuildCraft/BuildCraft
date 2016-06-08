package buildcraft.api.bpt;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.misc.NBTUtils;

public abstract class SchematicBlock extends Schematic {
    public final Block block;
    protected IBlockState state;

    public SchematicBlock(IBlockState state) {
        this.block = state.getBlock();
        this.state = state;
    }

    public SchematicBlock(Block block) {
        this.block = block;
        this.state = block.getDefaultState();
    }

    public SchematicBlock(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        this.block = state.getBlock();
        this.state = state;
    }

    public SchematicBlock(NBTTagCompound nbt) throws SchematicException {
        String regName = nbt.getString("block");
        this.block = Block.REGISTRY.getObject(new ResourceLocation(regName));
        if (block == Blocks.AIR && !"minecraft:air".equals(regName)) {
            throw new SchematicException("Unknown block name " + regName);
        }
        this.state = block.getDefaultState();
        if (nbt.hasKey("state")) {
            this.state = NBTUtils.readBlockStateProperties(state, nbt.getCompoundTag("state"));
        }
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("block", block.getRegistryName().toString());
        if (state.getProperties().size() > 0) {
            nbt.setTag("state", NBTUtils.writeBlockStateProperties(state));
        }
        return nbt;
    }

    @Override
    public void mirror(Axis axis) {}

    @Override
    public void rotate(Axis axis, Rotation rotation) {
        if (axis == Axis.Y) {
            state = state.withRotation(rotation);
        }
    }
}
