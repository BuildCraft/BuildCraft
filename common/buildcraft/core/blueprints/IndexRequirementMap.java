package buildcraft.core.blueprints;

import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.Position;
import buildcraft.core.builders.BuildingSlotBlock;

public class IndexRequirementMap {
	private final Multimap<BlockIndex, BlockIndex> requirements = HashMultimap.create();
	private final Multimap<BlockIndex, BlockIndex> requirementsInv = HashMultimap.create();

	public IndexRequirementMap() {

	}

	public void add(BuildingSlotBlock b, IBuilderContext context) {
		if (b.schematic instanceof SchematicBlock) {
			BlockIndex index = new BlockIndex(b.x, b.y, b.z);
			Set<BlockIndex> prereqs = ((SchematicBlock) b.schematic).getPrerequisiteBlocks(context);

			if (prereqs != null && prereqs.size() > 0) {
				Position min = context.surroundingBox().pMin();
				Position max = context.surroundingBox().pMax();
				for (BlockIndex i : prereqs) {
					BlockIndex ia = new BlockIndex(i.x + index.x, i.y + index.y, i.z + index.z);
					if (ia.equals(index) || ia.x < min.x || ia.y < min.y || ia.z < min.z || ia.x > max.x || ia.y > max.y || ia.z > max.z) {
						continue;
					}
					requirements.put(index, ia);
					requirementsInv.put(ia, index);
				}
			}
		}
	}

	public boolean contains(BlockIndex index) {
		return requirements.containsKey(index);
	}

	public void remove(BuildingSlotBlock b) {
		BlockIndex index = new BlockIndex(b.x, b.y, b.z);
		remove(index);
	}

	public void remove(BlockIndex index) {
		for (BlockIndex reqingIndex : requirementsInv.get(index)) {
			requirements.remove(reqingIndex, index);
		}
		requirementsInv.removeAll(index);
	}
}
