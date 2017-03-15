package buildcraft.builders.snapshot;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SchematicBlock implements INBTSerializable<NBTTagCompound> {
    public BlockPos relativePos;
    public Set<BlockPos> requiredBlockOffsets;
    public List<ItemStack> requiredItems;
    public List<IProperty<?>> ignoredProperties;
    public IBlockState blockState;
    public NBTTagCompound tileNbt;
    public Block placeBlock;
    public Set<Block> canBeReplacedWithBlocks;

    public SchematicBlock(
            BlockPos relativePos,
            Set<BlockPos> requiredBlockOffsets,
            List<ItemStack> requiredItems,
            List<IProperty<?>> ignoredProperties,
            IBlockState blockState,
            NBTTagCompound tileNbt,
            Block placeBlock,
            Set<Block> canBeReplacedWithBlocks
    ) {
        this.relativePos = relativePos;
        this.requiredBlockOffsets = requiredBlockOffsets;
        this.requiredItems = requiredItems;
        this.ignoredProperties = ignoredProperties;
        this.blockState = blockState;
        this.tileNbt = tileNbt;
        this.placeBlock = placeBlock;
        this.canBeReplacedWithBlocks = canBeReplacedWithBlocks;
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
        schematicBlock.relativePos = relativePos.rotate(rotation);
        schematicBlock.requiredBlockOffsets = requiredBlockOffsets.stream()
                .map(blockPos -> blockPos.rotate(rotation))
                .collect(Collectors.toCollection(HashSet::new));
        schematicBlock.requiredItems = requiredItems;
        schematicBlock.ignoredProperties = ignoredProperties;
        schematicBlock.blockState = blockState.withRotation(rotation);
        schematicBlock.tileNbt = tileNbt;
        schematicBlock.placeBlock = placeBlock;
        schematicBlock.canBeReplacedWithBlocks = canBeReplacedWithBlocks;
        return schematicBlock;
    }

    public boolean build(World world, BlockPos blockPos) {
        IBlockState newBlockState = blockState;
        if (placeBlock != blockState.getBlock()) {
            newBlockState = placeBlock.getDefaultState();
            for (IProperty<?> property : blockState.getPropertyKeys()) {
                if (newBlockState.getPropertyKeys().contains(property)) {
                    newBlockState = BlockUtil.copyProperty(
                            property,
                            newBlockState,
                            blockState
                    );
                }
            }
        }
        for (IProperty<?> property : ignoredProperties) {
            newBlockState = BlockUtil.copyProperty(
                    property,
                    newBlockState,
                    placeBlock.getDefaultState()
            );
        }
        if (world.setBlockState(blockPos, newBlockState)) {
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
