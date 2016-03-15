package buildcraft.builders.json;

import buildcraft.api.blueprints.BuildingPermission;
import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

import java.util.*;

public class BuilderSupportEntry {
	public class NBTEntry {
		public List<String> blacklist, whitelist, equal;

		public void validate(BuilderSupportEntry e) throws JSONValidationException {
			if (blacklist != null && whitelist != null) {
				throw new JSONValidationException(e, "Must not provide NBT tag blacklist when whitelist is already provided!");
			}
		}
	}

	public List<String> includes;

	@IncludeIgnore
	public String name;

	@IncludeIgnore
	public List<String> names;

	public List<String> equalProperties;
	// TODO
	public List<String> forcedProperties;
	// TODO
	public List<String> bannedProperties;
	public List<String> tileId;

	@IncludeRecurse
	public NBTEntry nbt;

	public String placedBlock;
	public List<String> requirements;
	public boolean ignore;
	public boolean ignoreDrops;
	public boolean ignoreInventoryContents;

	public BuildingPermission buildingPermission;
	public List<int[]> prerequisites;

	@IncludeRecurse
	public BuilderRotation rotation = null;
	public List<BuilderRotation> rotations = null;

	public transient int listPos;

	private transient Set<BlockPos> prerequisiteSet;

	public boolean isValidForState(IBlockState state) {
		// TODO
		return true;
	}

	public boolean isValidForTile(String id) {
		if (id == null && tileId != null) {
			return false;
		}
		return tileId != null ? tileId.contains(id) : true;
	}

	public Collection<BuilderRotation> getAllRotations() {
		return rotations != null ? rotations : (rotation != null ? Sets.newHashSet(rotation) : new HashSet<BuilderRotation>());
	}

	public Set<BlockPos> getPrerequisites() {
		return prerequisiteSet;
	}

	public void validate(BuilderSupportEntry e) throws JSONValidationException {
		if (name != null && names != null) {
			throw new JSONValidationException(e, "Must not provide both name and names list!");
		}

		if (buildingPermission == null) {
			buildingPermission = BuildingPermission.ALL;
		}

		if (nbt != null) {
			nbt.validate(e);
		}

		if (rotation != null) {
			if (rotations != null) {
				throw new JSONValidationException(e, "Must not provide both rotation list and entry!");
			}
			rotation.validate(e);
		} else if (rotations != null) {
			for (BuilderRotation r : rotations) {
				r.validate(e);
			}
		}

		if (requirements != null) {
			for (String s : requirements) {
				BuilderSupportUtils.validateItemStack(e, s);
			}
		}

		if (placedBlock != null) {
			BuilderSupportUtils.validateItemStack(e, placedBlock);
		}

		if (prerequisites != null) {
			for (int[] p : prerequisites) {
				prerequisiteSet = new HashSet<BlockPos>();
				if (p == null || p.length != 3) {
					throw new JSONValidationException(e, "Invalid prerequisite length: " + (p != null ? p.length : "null") + "!");
				}

				prerequisiteSet.add(new BlockPos(p[0], p[1], p[2]));
			}
		} else {
			prerequisiteSet = null;
		}
	}
}
