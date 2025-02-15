package buildcraft.energy.generation.structure;

import buildcraft.energy.BCEnergy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class OilStructureRegistry {
    public static String STRUCTURE_OIL_SPOUT = "oil_spout";
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(BCEnergy.MODID, STRUCTURE_OIL_SPOUT);

    public static final StructureType<OilStructureFeature> STRUCTURE_TYPE = StructureType.register(STRUCTURE_ID.toString(), OilStructureFeature.CODEC);

    public static final StructurePieceType STRUCTURE_PIECE_TYPE = StructurePieceType.setPieceId(
            OilStructure::deserialize,
            STRUCTURE_ID.toString()
    );

    public static void cinit() {
    }
}
