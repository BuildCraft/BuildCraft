package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Blueprint extends Snapshot {
    public SchematicBlock[][][] data;
    public List<SchematicEntity> entities;

    @Override
    public <T extends ITileForSnapshotBuilder> SnapshotBuilder<T> createBuilder(T tile) {
        // noinspection unchecked
        return (SnapshotBuilder<T>) new BlueprintBuilder((ITileForBlueprintBuilder) tile);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        SchematicBlock[] serializedData = new SchematicBlock[size.getX() * size.getY() * size.getZ()];
        int i = 0;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    serializedData[i++] = data[x][y][z];
                }
            }
        }
        nbt.setTag("data", NBTUtilBC.writeCompoundList(Stream.of(serializedData).map(SchematicBlock::serializeNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        data = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
        SchematicBlock[] serializedData = NBTUtilBC.readCompoundList(nbt.getTagList("data", Constants.NBT.TAG_COMPOUND))
                .map(schematicBlockTag -> {
                    SchematicBlock schematicBlock = new SchematicBlock();
                    schematicBlock.deserializeNBT(schematicBlockTag);
                    return schematicBlock;
                })
                .toArray(SchematicBlock[]::new);
        int i = 0;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    data[x][y][z] = serializedData[i++];
                }
            }
        }
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
        public final int maxLevel;

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.rotation = rotation;
//            for (int i = 0; i < 1000; i++) {
            SchematicBlockFactory.computeRequired(getSnapshot());
//            }
            for (int z = 0; z < getSnapshot().size.getZ(); z++) {
                for (int y = 0; y < getSnapshot().size.getY(); y++) {
                    for (int x = 0; x < getSnapshot().size.getX(); x++) {
                        SchematicBlock schematicBlock = data[x][y][z];
                        BlockPos blockPos = new BlockPos(x, y, z).rotate(rotation)
                                .add(basePos)
                                .add(offset.rotate(rotation));
                        if (schematicBlock.blockState.getBlock().isAir(schematicBlock.blockState, null, null)) {
                            toBreak.add(blockPos);
                        } else {
                            toPlace.put(blockPos, schematicBlock.getRotated(rotation));
                        }
                    }
                }
            }
            box = new Box();
            Stream.concat(toBreak.stream(), toPlace.keySet().stream()).forEach(box::extendToEncompass);
            maxLevel = toPlace.values().stream().mapToInt(schematicBlock -> schematicBlock.level).max().orElse(0);
        }

        public Blueprint getSnapshot() {
            return Blueprint.this;
        }

        public Box getBox() {
            return box;
        }
    }
}
