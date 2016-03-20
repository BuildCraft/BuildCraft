package buildcraft.core.blueprints;

import java.util.Set;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.lib.utils.Utils;

public class IndexRequirementMap {
    private final Multimap<BlockPos, BlockPos> requirements = HashMultimap.create();
    private final Multimap<BlockPos, BlockPos> requirementsInv = HashMultimap.create();

    public IndexRequirementMap() {

    }

    public void add(BuildingSlotBlock b, IBuilderContext context) {
        if (b.schematic instanceof SchematicBlock) {
            BlockPos index = b.pos;
            Set<BlockPos> prereqs = ((SchematicBlock) b.schematic).getPrerequisiteBlocks(context);

            if (prereqs != null && prereqs.size() > 0) {
                for (BlockPos i : prereqs) {
                    BlockPos ia = i.add(index);

                    if (ia.equals(index) || !context.surroundingBox().contains(Utils.convert(ia))) {
                        continue;
                    }
                    requirements.put(index, ia);
                    requirementsInv.put(ia, index);
                }
            }
        }
    }

    public boolean contains(BlockPos index) {
        return requirements.containsKey(index);
    }

    public void remove(BuildingSlotBlock b) {
        remove(b.pos);
    }

    public void remove(BlockPos index) {
        for (BlockPos reqingIndex : requirementsInv.get(index)) {
            requirements.remove(reqingIndex, index);
        }
        requirementsInv.removeAll(index);
    }
}
