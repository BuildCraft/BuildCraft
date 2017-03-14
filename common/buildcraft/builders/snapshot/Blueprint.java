package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Stream;

public class Blueprint extends Snapshot {
    public final List<SchematicBlock> schematicBlocks = new ArrayList<>();

    @Override
    public <T extends ITileForSnapshotBuilder> SnapshotBuilder<T> createBuilder(T tile) {
        // noinspection unchecked
        return (SnapshotBuilder<T>) new BlueprintBuilder((ITileForBlueprintBuilder) tile);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setTag("data", NBTUtilBC.writeCompoundList(schematicBlocks.stream().map(SchematicBlock::serializeNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        NBTUtilBC.readCompoundList(nbt.getTagList("data", Constants.NBT.TAG_COMPOUND)).map(schematicBlockTag -> {
            SchematicBlock schematicBlock = new SchematicBlock();
            schematicBlock.deserializeNBT(schematicBlockTag);
            return schematicBlock;
        }).forEach(schematicBlocks::add);
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.BLUEPRINT;
    }

    public class BuildingInfo {
        public final BlockPos basePos;
        public final Rotation rotation;
        public final Box box;
        public final List<BlockPos> toBreak = new ArrayList<>();
        public final Map<BlockPos, SchematicBlock> toPlace = new HashMap<>();

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.rotation = rotation;
            for (SchematicBlock schematicBlock : schematicBlocks) {
                BlockPos blockPos = schematicBlock.relativePos.rotate(rotation).add(basePos);
                if (schematicBlock.blockState.getBlock().isAir(schematicBlock.blockState, null, null)) {
                    toBreak.add(blockPos);
                } else {
                    toPlace.put(blockPos, schematicBlock.getRotated(rotation));
                }
            }
            box = new Box();
            Stream.concat(toBreak.stream(), toPlace.keySet().stream()).forEach(box::extendToEncompass);
        }

        public Blueprint getSnapshot() {
            return Blueprint.this;
        }

        public Box getBox() {
            return box;
        }
    }
}
