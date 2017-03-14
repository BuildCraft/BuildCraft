package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

public class SchematicBlock implements INBTSerializable<NBTTagCompound> {
    public BlockPos relativePos;
    public Set<BlockPos> requiredBlockOffsets;
    public List<ItemStack> requiredItems;
    public IBlockState blockState;
    public NBTTagCompound tileNbt;

    public SchematicBlock(
            BlockPos relativePos,
            Set<BlockPos> requiredBlockOffsets,
            List<ItemStack> requiredItems,
            IBlockState blockState,
            NBTTagCompound tileNbt
    ) {
        this.relativePos = relativePos;
        this.requiredBlockOffsets = requiredBlockOffsets;
        this.requiredItems = requiredItems;
        this.blockState = blockState;
        this.tileNbt = tileNbt;
    }

    public SchematicBlock() {
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("relativePos", NBTUtil.createPosTag(relativePos));
        NBTTagCompound blockStateTag = new NBTTagCompound();
        NBTUtil.writeBlockState(blockStateTag, blockState);
        nbt.setTag("requiredItems", NBTUtilBC.writeCompoundList(requiredItems.stream().map(ItemStack::serializeNBT)));
        nbt.setTag("blockState", blockStateTag);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        relativePos = NBTUtil.getPosFromTag(nbt.getCompoundTag("relativePos"));
        NBTUtilBC.readCompoundList(nbt.getTagList("requiredItems", Constants.NBT.TAG_COMPOUND))
                .map(ItemStack::new)
                .forEach(requiredItems::add);
        blockState = NBTUtil.readBlockState(nbt.getCompoundTag("blockState"));
    }

    public SchematicBlock getRotated(Rotation rotation) {
        SchematicBlock schematicBlock = new SchematicBlock();
        schematicBlock.relativePos = RotationUtil.rotateBlockPos(relativePos, rotation);
        schematicBlock.requiredBlockOffsets = requiredBlockOffsets;
        schematicBlock.requiredItems = requiredItems;
        schematicBlock.blockState = blockState.withRotation(rotation);
        schematicBlock.tileNbt = tileNbt;
        return schematicBlock;
    }

    public boolean build(World world, BlockPos blockPos) {
        if (world.setBlockState(blockPos, blockState)) {
            if (tileNbt != null && blockState.getBlock().hasTileEntity(blockState)) {
                NBTTagCompound newTileNbt = new NBTTagCompound();
                tileNbt.getKeySet().stream()
                        .map(key -> Pair.of(key, tileNbt.getTag(key)))
                        .forEach(kv -> newTileNbt.setTag(kv.getKey(), kv.getValue()));
                newTileNbt.setInteger("x", blockPos.getX());
                newTileNbt.setInteger("y", blockPos.getY());
                newTileNbt.setInteger("z", blockPos.getZ());
                TileEntity tileEntity = TileEntity.create(world, newTileNbt);
                if (tileEntity != null) {
                    tileEntity.setWorld(world);
                    world.setTileEntity(blockPos, tileEntity);
                }
                return true;
            }
        }
        return false;
    }
}
