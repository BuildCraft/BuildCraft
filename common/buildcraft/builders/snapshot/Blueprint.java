package buildcraft.builders.snapshot;

import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Blueprint extends Snapshot {
    public ISchematicBlock<?>[][][] data;
    public List<ISchematicEntity<?>> entities;

    @Override
    public <T extends ITileForSnapshotBuilder> SnapshotBuilder<T> createBuilder(T tile) {
        // noinspection unchecked
        return (SnapshotBuilder<T>) new BlueprintBuilder((ITileForBlueprintBuilder) tile);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        ISchematicBlock<?>[] serializedData = new ISchematicBlock<?>[size.getX() * size.getY() * size.getZ()];
        int i = 0;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    serializedData[i++] = data[x][y][z];
                }
            }
        }
        nbt.setTag("data", NBTUtilBC.writeCompoundList(Stream.of(serializedData).map(SchematicBlockManager::writeToNBT)));
        nbt.setTag("entities", NBTUtilBC.writeCompoundList(entities.stream().map(SchematicEntityManager::writeToNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        data = new ISchematicBlock<?>[size.getX()][size.getY()][size.getZ()];
        ISchematicBlock<?>[] serializedData = NBTUtilBC.readCompoundList(nbt.getTagList("data", Constants.NBT.TAG_COMPOUND))
            .map(SchematicBlockManager::readFromNBT)
            .toArray(ISchematicBlock<?>[]::new);
        int i = 0;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    data[x][y][z] = serializedData[i++];
                }
            }
        }
        entities = NBTUtilBC.readCompoundList(nbt.getTagList("entities", Constants.NBT.TAG_COMPOUND))
            .map(SchematicEntityManager::readFromNBT)
            .collect(Collectors.toList());
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
        public final Map<BlockPos, ISchematicBlock<?>> toPlace = new HashMap<>();
        public final List<ISchematicEntity<?>> entities = new ArrayList<>();
        public final int maxLevel;

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.rotation = rotation;
            SchematicBlockManager.computeRequired(getSnapshot());
            SchematicEntityManager.computeRequired(getSnapshot());
            for (int z = 0; z < getSnapshot().size.getZ(); z++) {
                for (int y = 0; y < getSnapshot().size.getY(); y++) {
                    for (int x = 0; x < getSnapshot().size.getX(); x++) {
                        ISchematicBlock<?> schematicBlock = data[x][y][z];
                        BlockPos blockPos = new BlockPos(x, y, z).rotate(rotation)
                            .add(basePos)
                            .add(offset.rotate(rotation));
                        if (schematicBlock.isAir()) {
                            toBreak.add(blockPos);
                        } else {
                            toPlace.put(blockPos, schematicBlock.getRotated(rotation));
                        }
                    }
                }
            }
            getSnapshot().entities.stream()
                .map(schematicEntity -> schematicEntity.getRotated(rotation))
                .forEach(entities::add);
            box = new Box();
            Stream.concat(toBreak.stream(), toPlace.keySet().stream()).forEach(box::extendToEncompass);
            maxLevel = toPlace.values().stream().mapToInt(ISchematicBlock::getLevel).max().orElse(0);
        }

        public Blueprint getSnapshot() {
            return Blueprint.this;
        }

        public Box getBox() {
            return box;
        }
    }
}
