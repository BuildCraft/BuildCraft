package buildcraft.energy.generation.structure;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OilStructure extends StructurePiece {
    public final List<OilGenStructurePart> pieces;
    public final Box containingBox;

    public OilStructure(Box containingBox, List<OilGenStructurePart> pieces) {
        super(OilStructureRegistry.STRUCTURE_PIECE_TYPE, 0, containingBox.getBB());
        this.pieces = pieces;
        this.containingBox = containingBox;
    }

    // Generate
    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureFeatureManager featureManager,
            ChunkGenerator chunkGeneratorIn,
            Random rand,
            BoundingBox bounds,
            ChunkPos chunkPos,
            BlockPos centerPos
    ) {
        OilPlacer placer = new OilPlacer(level, this.pieces, bounds);
        placer.place();
    }

    public boolean isEmpty() {
        return pieces.isEmpty();
    }

    // Save as NBT
    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        ListTag list = new ListTag();
        for (OilGenStructurePart piece : this.pieces) {
            CompoundTag tagOfPiece = new CompoundTag();
            CompoundTag fields = new CompoundTag();
            if (piece.getClass() == OilGenStructurePart.Spring.class) {
                tagOfPiece.putString("class", "Spring");
                IntArrayTag pos = NBTUtilBC.writeBlockPos(((OilGenStructurePart.Spring) piece).pos);
                fields.put("pos", pos);
            } else if (piece.getClass() == OilGenStructurePart.Spout.class) {
                tagOfPiece.putString("class", "Spout");
                IntArrayTag start = NBTUtilBC.writeBlockPos(((OilGenStructurePart.Spout) piece).start);
                fields.put("start", start);
                fields.putInt("radius", ((OilGenStructurePart.Spout) piece).radius);
                fields.putInt("height", ((OilGenStructurePart.Spout) piece).height);
            } else if (piece.getClass() == OilGenStructurePart.GenByPredicate.class) {
                tagOfPiece.putString("class", "GenByPredicate");
                Object[] predicateArgs = ((OilGenStructurePart.GenByPredicate) piece).predicateArgs;
                // BlockPos double
                // center, radiusSq
                if (predicateArgs.length == 2) {
                    fields.put("center", NBTUtilBC.writeBlockPos((BlockPos) predicateArgs[0]));
                    fields.putDouble("radiusSq", (double) predicateArgs[1]);
                }
                // Axis int BlockPos double
                // axis, toReplace, center, radiusSq
                else if (predicateArgs.length == 4) {
                    fields.putString("axis", ((Direction.Axis) predicateArgs[0]).name());
                    fields.putInt("toReplace", (int) predicateArgs[1]);
                    fields.put("center", NBTUtilBC.writeBlockPos((BlockPos) predicateArgs[2]));
                    fields.putDouble("radiusSq", (double) predicateArgs[3]);
                } else {
                    throw new RuntimeException("Unexpected Predicate Args Length!");
                }
            } else if (piece.getClass() == OilGenStructurePart.FlatPattern.class) {
                tagOfPiece.putString("class", "FlatPattern");
                ListTag pattern = new ListTag();
                for (boolean[] row : ((OilGenStructurePart.FlatPattern) piece).pattern) {
                    ListTag tagRow = NBTUtilBC.writeBooleanArray(row);
                    pattern.add(tagRow);
                }
                fields.put("pattern", pattern);
                fields.putInt("depth", ((OilGenStructurePart.FlatPattern) piece).depth);
            } else if (piece.getClass() == OilGenStructurePart.PatternTerrainHeight.class) {
                tagOfPiece.putString("class", "PatternTerrainHeight");
                ListTag pattern = new ListTag();
                for (boolean[] row : ((OilGenStructurePart.PatternTerrainHeight) piece).pattern) {
                    ListTag tagRow = NBTUtilBC.writeBooleanArray(row);
                    pattern.add(tagRow);
                }
                fields.put("pattern", pattern);
                fields.putInt("depth", ((OilGenStructurePart.PatternTerrainHeight) piece).depth);
            } else {
                throw new RuntimeException("Unexcepted Oil Structure Type!");
            }
            // replaceType
            fields.putString("replaceType", piece.replaceType.name());
            // box
            CompoundTag box = new CompoundTag();
            IntArrayTag box_max = NBTUtilBC.writeBlockPos(piece.box.max());
            box.put("box_max", box_max);
            IntArrayTag box_min = NBTUtilBC.writeBlockPos(piece.box.min());
            box.put("box_min", box_min);
            fields.put("box", box);
            // all fields above
            tagOfPiece.put("fields", fields);
            list.add(tagOfPiece);
        }
        // box
        CompoundTag box = new CompoundTag();
        IntArrayTag containingBox_max = NBTUtilBC.writeBlockPos(containingBox.max());
        box.put("containingBox_max", containingBox_max);
        IntArrayTag containingBox_min = NBTUtilBC.writeBlockPos(containingBox.min());
        box.put("containingBox_min", containingBox_min);
        tag.put("containingBox", box);

        tag.put("oil_structure_parts", list.copy());
    }

    // Load from NBT
    public static OilStructure deserialize(CompoundTag tag) {
        List<OilGenStructurePart> pieces = new ArrayList<>();
        ListTag pieceTags = tag.getList("oil_structure_parts", Tag.TAG_COMPOUND);
        for (Tag t : pieceTags) {
            OilGenStructurePart currentPart;
            if (t instanceof CompoundTag tagOfPiece) {
                CompoundTag fields = tagOfPiece.getCompound("fields");
                // replaceType
                OilGenStructurePart.ReplaceType type = OilGenStructurePart.ReplaceType.valueOf(fields.getString("replaceType"));
                // box
                CompoundTag boxTag = fields.getCompound("box");
                BlockPos box_max = NBTUtilBC.readBlockPos(boxTag.get("box_max"));
                BlockPos box_min = NBTUtilBC.readBlockPos(boxTag.get("box_min"));
                Box box = new Box(box_min, box_max);
                // all fields above
                switch (tagOfPiece.getString("class")) {
                    case "Spring":
                        BlockPos pos = NBTUtilBC.readBlockPos(fields.get("pos"));
                        currentPart = new OilGenStructurePart.Spring(pos);
                        break;
                    case "Spout":
                        BlockPos start = NBTUtilBC.readBlockPos(fields.get("start"));
                        int radius = fields.getInt("radius");
                        int height = fields.getInt("height");
                        currentPart = new OilGenStructurePart.Spout(start, type, radius, height);
                        break;
                    case "GenByPredicate":
                        BlockPos center;
                        double radiusSq;
                        switch (fields.getAllKeys().size()) {
                            // 2->replaceType box
                            // BlockPos double
                            // center, radiusSq
                            case 2 + 2:
                                center = NBTUtilBC.readBlockPos(fields.get("center"));
                                radiusSq = fields.getDouble("radiusSq");
                                currentPart = new OilGenStructurePart.GenByPredicate(box, type, center, radiusSq);
                                break;
                            // Axis int BlockPos double
                            // axis, toReplace, center, radiusSq
                            case 4 + 2:
                                Direction.Axis axis = Direction.Axis.valueOf(fields.getString("axis"));
                                int toReplace = fields.getInt("toReplace");
                                center = NBTUtilBC.readBlockPos(fields.get("center"));
                                radiusSq = fields.getDouble("radiusSq");
                                currentPart = new OilGenStructurePart.GenByPredicate(box, type, axis, toReplace, center, radiusSq);
                                break;
                            default:
                                throw new RuntimeException("Unexpected Predicate Args Length!");
                        }
                        break;
                    case "FlatPattern":
                        List<boolean[]> patternListOuterFlatPattern = new ArrayList<>();
                        ListTag patternListTagOuter = fields.getList("pattern", Tag.TAG_LIST);
                        for (Tag rowTag : patternListTagOuter) {
                            if (rowTag instanceof ListTag rowListTag) {
                                boolean[] row = NBTUtilBC.readBooleanArray(rowListTag);
                                patternListOuterFlatPattern.add(row);
                            } else {
                                throw new RuntimeException("Unexpected FlatPattern Pattern Tag Type!");
                            }
                        }
                        int depthFlatPattern = fields.getInt("depth");
                        currentPart = new OilGenStructurePart.FlatPattern(box, type, (boolean[][]) patternListOuterFlatPattern.stream().toArray(), depthFlatPattern);
                        break;
                    case "PatternTerrainHeight":
                        List<boolean[]> patternListOuterPatternTerrainHeight = new ArrayList<>();
                        ListTag patternListTagOuterPatternTerrainHeight = fields.getList("pattern", Tag.TAG_LIST);
                        for (Tag rowTag : patternListTagOuterPatternTerrainHeight) {
                            if (rowTag instanceof ListTag rowListTag) {
                                boolean[] row = NBTUtilBC.readBooleanArray(rowListTag);
                                patternListOuterPatternTerrainHeight.add(row);
                            } else {
                                throw new RuntimeException("Unexpected FlatPattern Pattern Tag Type!");
                            }
                        }
                        int depthPatternTerrainHeight = fields.getInt("depth");
                        currentPart = new OilGenStructurePart.PatternTerrainHeight(
                                box,
                                type,
                                patternListOuterPatternTerrainHeight.toArray(new boolean[patternListOuterPatternTerrainHeight.size()][]),
                                depthPatternTerrainHeight
                        );
                        break;
                    default:
                        throw new RuntimeException("Unexpected Oil Structure Piece Type!");
                }
            } else {
                throw new RuntimeException("Only CompoundTag is Legal to Appear, What Happened?");
            }
            pieces.add(currentPart);
        }
        // box
        CompoundTag containingBoxTag = tag.getCompound("containingBox");
        BlockPos containingBox_max = NBTUtilBC.readBlockPos(containingBoxTag.get("containingBox_max"));
        BlockPos containingBox_min = NBTUtilBC.readBlockPos(containingBoxTag.get("containingBox_min"));
        Box box = new Box(containingBox_min, containingBox_max);
        return new OilStructure(box, pieces);
    }
}
