package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public class SchematicBlock implements INBTSerializable<NBTTagCompound> {
    public BlockPos relativePos;
    public IBlockState blockState;
    public List<ItemStack> requiredItems;

    public SchematicBlock(BlockPos relativePos, IBlockState blockState, List<ItemStack> requiredItems) {
        this.relativePos = relativePos;
        this.blockState = blockState;
        this.requiredItems = requiredItems;
    }

    public SchematicBlock() {
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("relativePos", NBTUtil.createPosTag(relativePos));
        NBTTagCompound blockStateTag = new NBTTagCompound();
        NBTUtil.writeBlockState(blockStateTag, blockState);
        nbt.setTag("blockState", blockStateTag);
        nbt.setTag("requiredItems", NBTUtilBC.writeCompoundList(requiredItems.stream().map(ItemStack::serializeNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        relativePos = NBTUtil.getPosFromTag(nbt.getCompoundTag("relativePos"));
        blockState = NBTUtil.readBlockState(nbt.getCompoundTag("blockState"));
        NBTUtilBC.readCompoundList(nbt.getTagList("requiredItems", Constants.NBT.TAG_COMPOUND))
                .map(ItemStack::new)
                .forEach(requiredItems::add);
    }

    public SchematicBlock getRotated(Rotation rotation) {
        SchematicBlock schematicBlock = new SchematicBlock();
        schematicBlock.relativePos = RotationUtil.rotateBlockPos(relativePos, rotation);
        schematicBlock.blockState = blockState.withRotation(rotation);
        schematicBlock.requiredItems = requiredItems;
        return schematicBlock;
    }
}
