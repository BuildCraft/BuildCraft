package buildcraft.energy.generation.structure;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OilStructure extends StructurePiece {
    public final List<OilGenStructurePart> pieces;

    public OilStructure(Box containingBox, List<OilGenStructurePart> pieces) {
        super(OilStructureRegistry.STRUCTURE_PIECE_TYPE, 0);
        this.pieces = pieces;
        super.boundingBox = containingBox.toMutableBoundingBox();
    }

    // Generate
    @Override
    public boolean postProcess(
            ISeedReader level,
            StructureManager featureManager,
            ChunkGenerator chunkGeneratorIn,
            Random rand,
            MutableBoundingBox bounds,
            ChunkPos chunkPos,
            BlockPos centerPos
    ) {
        OilPlacer placer = new OilPlacer(level, this.pieces, new MutableBoundingBox(bounds));
        placer.place();
        return true;
    }

    public boolean isEmpty() {
        return pieces.isEmpty();
    }

    // Save as NBT
    @Override
    protected void addAdditionalSaveData(CompoundNBT tag) {
        ListNBT list = new ListNBT();
        for (OilGenStructurePart piece : this.pieces) {
            CompoundNBT tagOfPiece = new CompoundNBT();
            CompoundNBT fields = new CompoundNBT();
            if (piece.getClass() == OilGenStructurePart.Spring.class) {
                tagOfPiece.putString("class", "Spring");
                IntArrayNBT pos = NBTUtilBC.writeBlockPos(((OilGenStructurePart.Spring) piece).pos);
                fields.put("pos", pos);
            } else if (piece.getClass() == OilGenStructurePart.Spout.class) {
                tagOfPiece.putString("class", "Spout");
                IntArrayNBT start = NBTUtilBC.writeBlockPos(((OilGenStructurePart.Spout) piece).start);
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
                ListNBT pattern = new ListNBT();
                for (boolean[] row : ((OilGenStructurePart.FlatPattern) piece).pattern) {
                    ListNBT tagRow = NBTUtilBC.writeBooleanArray(row);
                    pattern.add(tagRow);
                }
                fields.put("pattern", pattern);
                fields.putInt("depth", ((OilGenStructurePart.FlatPattern) piece).depth);
            } else if (piece.getClass() == OilGenStructurePart.PatternTerrainHeight.class) {
                tagOfPiece.putString("class", "PatternTerrainHeight");
                ListNBT pattern = new ListNBT();
                for (boolean[] row : ((OilGenStructurePart.PatternTerrainHeight) piece).pattern) {
                    ListNBT tagRow = NBTUtilBC.writeBooleanArray(row);
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
            CompoundNBT box = new CompoundNBT();
            IntArrayNBT box_max = NBTUtilBC.writeBlockPos(piece.box.max());
            box.put("box_max", box_max);
            IntArrayNBT box_min = NBTUtilBC.writeBlockPos(piece.box.min());
            box.put("box_min", box_min);
            fields.put("box", box);
            // all fields above
            tagOfPiece.put("fields", fields);
            list.add(tagOfPiece);
        }
        // box
        tag.put("containingBox", new MutableBoundingBox(super.boundingBox).createTag());

        tag.put("oil_structure_parts", list.copy());
    }

    // Load from NBT
    public static OilStructure deserialize(TemplateManager templateManager, CompoundNBT tag) {
        List<OilGenStructurePart> pieces = new ArrayList<>();
        ListNBT pieceTags = tag.getList("oil_structure_parts", Constants.NBT.TAG_COMPOUND);
        for (INBT t : pieceTags) {
            OilGenStructurePart currentPart;
            if (t instanceof CompoundNBT) {
                CompoundNBT tagOfPiece = (CompoundNBT) t;
                CompoundNBT fields = tagOfPiece.getCompound("fields");
                // replaceType
                OilGenStructurePart.ReplaceType type = OilGenStructurePart.ReplaceType.valueOf(fields.getString("replaceType"));
                // box
                CompoundNBT boxTag = fields.getCompound("box");
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
                        ListNBT patternListTagOuter = fields.getList("pattern", Constants.NBT.TAG_LIST);
                        for (INBT rowTag : patternListTagOuter) {
                            if (rowTag instanceof ListNBT) {
                                ListNBT rowListTag = (ListNBT) rowTag;
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
                        ListNBT patternListTagOuterPatternTerrainHeight = fields.getList("pattern", Constants.NBT.TAG_LIST);
                        for (INBT rowTag : patternListTagOuterPatternTerrainHeight) {
                            if (rowTag instanceof ListNBT) {
                                ListNBT rowListTag = (ListNBT) rowTag;
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
                throw new RuntimeException("Only CompoundNBT is Legal to Appear, What Happened?");
            }
            pieces.add(currentPart);
        }
        // box
        Box box = new Box(new MutableBoundingBox(tag.getIntArray("containingBox")));
        return new OilStructure(box, pieces);
    }
}
