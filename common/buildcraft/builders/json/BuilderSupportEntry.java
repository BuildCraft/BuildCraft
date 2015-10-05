package buildcraft.builders.json;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.core.BlockIndex;

public class BuilderSupportEntry {
	public class NBTEntry {
		public List<String> blacklist, whitelist, equality;

		public void validate(BuilderSupportEntry e) throws JSONValidationException {
			if (blacklist != null && whitelist != null) {
				throw new JSONValidationException(e, "Must not provide NBT tag blacklist when whitelist is already provided!");
			}
		}
	}

	@IncludeIgnore
	public List<String> includes;

	@IncludeIgnore
	public String name;

	@IncludeIgnore
	public List<String> names;

	public List<Integer> metadata;
	public List<String> tileId;

	public int metadataMask = 15;
	public int metadataEqualityMask = 0;

	@IncludeRecurse
	public NBTEntry nbt;

	public String placedBlock;
	public List<String> requirements;
	public boolean ignore;
	public boolean ignoreDrops;
	public boolean ignoreInventoryContents;

	public BuildingPermission buildingPermission;
	public List<int[]> prerequisites;

	public boolean notifyBlockTwice = false;

	@IncludeRecurse
	public BuilderRotation rotation = null;
	public List<BuilderRotation> rotationList = null;

	public transient int listPos;

	private transient Set<BlockIndex> prerequisiteSet;

	public boolean isValidForMeta(int meta) {
		return metadata != null ? metadata.contains(meta) : true;
	}

	public boolean isValidForTile(String id) {
		if (id == null && tileId != null) {
			return false;
		}
		return tileId != null ? tileId.contains(id) : true;
	}

	public Collection<BuilderRotation> getAllRotations() {
		return rotationList != null ? rotationList : (rotation != null ? Sets.newHashSet(rotation) : new HashSet<BuilderRotation>());
	}

	public Set<BlockIndex> getPrerequisites() {
		return prerequisiteSet;
	}

	public void validate(BuilderSupportEntry e) throws JSONValidationException {
		if (name != null && names != null) {
			throw new JSONValidationException(e, "Must not provide both name and names list!");
		}

		if (buildingPermission == null) {
			buildingPermission = BuildingPermission.ALL;
		}

		if (metadata != null) {
			for (Integer i : metadata) {
				if (i < 0 || i >= 16) {
					throw new JSONValidationException(e, "Invalid metadata: " + i + "!");
				}
			}
		}

		if (nbt != null) {
			nbt.validate(e);
		}

		if (rotation != null) {
			if (rotationList != null) {
				throw new JSONValidationException(e, "Must not provide both rotation list and entry!");
			}
			rotation.validate(e);
		} else if (rotationList != null) {
			for (BuilderRotation r : rotationList) {
				r.validate(e);
			}
		}

		if (metadataEqualityMask == 0) {
			// Try to heurestically deduce a good metadata equality mask.
			for (BuilderRotation r : getAllRotations()) {
				if (r.type == BuilderRotation.Type.METADATA) {
					metadataEqualityMask |= r.mask;
				}
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
				prerequisiteSet = new HashSet<BlockIndex>();
				if (p == null || p.length != 3) {
					throw new JSONValidationException(e, "Invalid prerequisite length: " + (p != null ? p.length : "null") + "!");
				}

				prerequisiteSet.add(new BlockIndex(p[0], p[1], p[2]));
			}
		} else {
			prerequisiteSet = null;
		}
	}
}
