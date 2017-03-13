package buildcraft.builders.snapshot;

import buildcraft.lib.misc.data.Box;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blueprint extends Snapshot {
    public BlockPos size;
    public final List<SchematicBlock> schematicBlocks = new ArrayList<>();

    @Override
    public <T extends ITileForSnapshotBuilder> SnapshotBuilder<T> createBuilder(T tile) {
        // noinspection unchecked
        return (SnapshotBuilder<T>) new BlueprintBuilder((ITileForBlueprintBuilder) tile);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
//        nbt.setTag("data", NBTUtilBC.writeCompoundList(data.stream().map(SchematicBlock::serializeNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
//        NBTUtilBC.readCompoundList(nbt.getTagList("data", Constants.NBT.TAG_COMPOUND)).map(schematicBlockTag -> {
//            SchematicBlock schematicBlock = new SchematicBlock();
//            schematicBlock.deserializeNBT(schematicBlockTag);
//            return schematicBlock;
//        }).forEach(data::add);
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.BLUEPRINT;
    }

    public class BuildingInfo {
        public final BlockPos basePos;
        public final List<BlockPos> toBreak = new ArrayList<>();
        public final Map<BlockPos, SchematicBlock> toPlace = new HashMap<>();

        public BuildingInfo(BlockPos basePos) {
            this.basePos = basePos;
            for (SchematicBlock schematicBlock : schematicBlocks) {
                BlockPos blockPos = schematicBlock.relativePos.add(basePos);
                if (schematicBlock.blockState.getBlock().isAir(schematicBlock.blockState, null, null)) {
                    toBreak.add(blockPos);
                } else {
                    toPlace.put(blockPos, schematicBlock);
                }
            }
        }

        public Blueprint getSnapshot() {
            return Blueprint.this;
        }

        public Box getBox() {
            return new Box(basePos, basePos.add(getSnapshot().size));
        }
    }
}
