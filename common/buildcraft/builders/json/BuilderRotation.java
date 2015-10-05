package buildcraft.builders.json;

import java.util.Set;

import com.google.gson.annotations.SerializedName;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BlockIndex;
import buildcraft.core.lib.utils.NBTUtils;

public class BuilderRotation {
	enum Type {
		@SerializedName("metadata")
		METADATA,

		@SerializedName("nbtField")
		NBT_FIELD,

		@SerializedName("nbtRotateArray")
		NBT_ROTATE_ARRAY
	}

	private static final ForgeDirection[] MATRIX_ORDER = {
			ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST
	};

	public Type type;
	public int mask, shift;
	public boolean sticksToWall;
	private String tag;
	private int[] matrix, transformation;

	private transient boolean maskShifted;

	private int rotateLeft(int v) {
		int rot = mask != 0 ? v & mask : v;
		int out = v & ~mask;

		if (transformation != null) {
			for (int i = 0; i < transformation.length; i++) {
				if (rot == (i << shift)) {
					rot = transformation[i] << shift;
					break;
				}
			}
		} else {
			for (int i = 0; i < matrix.length; i++) {
				if (matrix[i] == rot) {
					rot = matrix[(i + 1) % matrix.length];
					if (mask != 0) {
						rot &= mask;
					}
					break;
				}
			}
		}

		return out | rot;
	}

	public ForgeDirection getOrientation(SchematicJSON s) {
		int pos = -1;
		int rot;

		if (type == Type.METADATA) {
			rot = s.meta;
		} else if (type == Type.NBT_FIELD) {
			NBTBase field = NBTUtils.getTag(s.tileNBT, tag);
			if (field == null) {
				return ForgeDirection.UNKNOWN;
			}
			rot = ((NBTBase.NBTPrimitive) field).func_150287_d();
		} else {
			return ForgeDirection.UNKNOWN;
		}

		rot = mask != 0 ? rot & mask : rot;
		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i] == rot) {
				pos = i;
				break;
			}
		}

		if (pos >= 0 && pos < 4) {
			return MATRIX_ORDER[pos];
		} else {
			return ForgeDirection.UNKNOWN;
		}
	}

	public void rotateLeft(SchematicJSON s, IBuilderContext context) {
		if (type == BuilderRotation.Type.METADATA) {
			s.meta = rotateLeft(s.meta);
		} else if (type == BuilderRotation.Type.NBT_ROTATE_ARRAY) {
			NBTBase sidedArray = NBTUtils.getTag(s.tileNBT, tag);
			if (sidedArray == null) {
				BCLog.logger.warn("Could not find field " + tag + " in tile " + s.tileNBT.getString("id") + "!");
				return;
			} else if (sidedArray instanceof NBTTagByteArray) {
				byte[] rotated = ((NBTTagByteArray) sidedArray).func_150292_c();
				byte[] rotatedNew = new byte[rotated.length];
				for (int i = 0; i < rotated.length; i++) {
					if (i >= transformation.length || transformation[i] >= rotated.length) {
						rotatedNew[i] = rotated[i];
					} else {
						rotatedNew[transformation[i]] = rotated[i];
					}
				}
				NBTUtils.setTag(s.tileNBT, tag, new NBTTagByteArray(rotatedNew));
			} else if (sidedArray instanceof NBTTagIntArray) {
				int[] rotated = ((NBTTagIntArray) sidedArray).func_150302_c();
				int[] rotatedNew = new int[rotated.length];
				for (int i = 0; i < rotated.length; i++) {
					if (i >= transformation.length || transformation[i] >= rotated.length) {
						rotatedNew[i] = rotated[i];
					} else {
						rotatedNew[transformation[i]] = rotated[i];
					}
				}
				NBTUtils.setTag(s.tileNBT, tag, new NBTTagIntArray(rotatedNew));
			} else if (sidedArray instanceof NBTTagList) {
				NBTTagList list = (NBTTagList) sidedArray;
				NBTBase[] tags = new NBTBase[list.tagCount()];
				for (int i = 0; i < tags.length; i++) {
					if (i >= transformation.length || transformation[i] >= tags.length) {
						tags[i] = list.getCompoundTagAt(i);
					} else {
						tags[transformation[i]] = list.getCompoundTagAt(i);
					}
				}
				NBTTagList listNew = new NBTTagList();
				for (int i = 0; i < tags.length; i++) {
					listNew.appendTag(tags[i]);
				}
				NBTUtils.setTag(s.tileNBT, tag, listNew);
			}
		} else if (type == BuilderRotation.Type.NBT_FIELD) {
			NBTBase field = NBTUtils.getTag(s.tileNBT, tag);
			if (field == null) {
				BCLog.logger.warn("Could not find field " + tag + " in tile " + s.tileNBT.getString("id") + "!");
				return;
			}
			int i = rotateLeft(((NBTBase.NBTPrimitive) field).func_150287_d());
			if (field instanceof NBTTagByte) {
				NBTUtils.setTag(s.tileNBT, tag, new NBTTagByte((byte) i));
			} else if (field instanceof NBTTagShort) {
				NBTUtils.setTag(s.tileNBT, tag, new NBTTagShort((short) i));
			} else if (field instanceof NBTTagInt) {
				NBTUtils.setTag(s.tileNBT, tag, new NBTTagInt(i));
			}
		}
	}

	public void validate(BuilderSupportEntry e) throws JSONValidationException {
		if (type == null || matrix == null) {
			throw new JSONValidationException(e, "Invalid rotation type!");
		}

		if (matrix == null && sticksToWall) {
			throw new JSONValidationException(e, "Must set rotation matrix (N->E->S->W) if block sticks to wall!");
		}

		if (shift < 0 || shift > 32) {
			throw new JSONValidationException(e, "Invalid shift value specified: " + shift + "!");
		}

		if (!maskShifted) {
			mask <<= shift;
			maskShifted = true;
		}

		if (type == Type.METADATA) {
			if (tag != null) {
				throw new JSONValidationException(e, "Must not specify tag when using s.metadata type!");
			}
			if ((mask & ~15) != 0) {
				throw new JSONValidationException(e, "Mask out of bounds!");
			}
		} else if (type == Type.NBT_FIELD || type == Type.NBT_ROTATE_ARRAY) {
			if (tag == null) {
				throw new JSONValidationException(e, "Must specify tag when using NBT type!");
			}
			if (type == Type.NBT_ROTATE_ARRAY) {
				if (transformation == null) {
					throw new JSONValidationException(e, "Must specify transformation when using NBT array rotation!");
				}
			}
		}

		if (sticksToWall) {
			if (matrix.length != 4) {
				throw new JSONValidationException(e, "SticksToWall only works for matrices of length 4 (N->E->S->W)!");
			} else if (type == Type.NBT_ROTATE_ARRAY) {
				throw new JSONValidationException(e, "SticksToWall does not work for NBTRotateArray!");
			}
		}
	}
}
