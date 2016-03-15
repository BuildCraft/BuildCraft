package buildcraft.builders.json;

import com.google.gson.annotations.SerializedName;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.core.BCLog;
import buildcraft.core.lib.utils.NBTUtils;

public class BuilderRotation {
	enum Axis {
		Y,
		Z,
		X
	}

	enum Type {
		@SerializedName("metadata")
		METADATA,

		@SerializedName("nbtField")
		NBT_FIELD,

		@SerializedName("nbtRotateArray")
		NBT_ROTATE_ARRAY,

		@SerializedName("nbtBitShift")
		NBT_BITWISE_SHIFT
	}

	private static final ForgeDirection[] MATRIX_ORDER = {
			ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST
	};

	public Type type;
	public int mask, shift;
	public boolean sticksToWall;

	private String format;
	private String tag;
	private int[] matrix, transformation;

	private transient boolean initialized;

	// TODO (7.2.x): Make use of me!
	private int flip(int v, Axis axis) {
		int rot = mask != 0 ? v & mask : v;
		int out = v & ~mask;

		if (format.equals("forgedirection")) {
			if (rot == (axis.ordinal() << (1 + shift)) || rot == (((axis.ordinal() << 1) + 1) << shift)) {
				rot ^= (1 << shift);
			}
		} else if (matrix.length == 4) {
			switch (axis) {
				case X:
					if (rot == (matrix[1] << shift)) {
						rot = matrix[3] << shift;
					} else if (rot == (matrix[3] << shift)) {
						rot = matrix[1] << shift;
					}
				case Z:
					if (rot == (matrix[0] << shift)) {
						rot = matrix[2] << shift;
					} else if (rot == (matrix[2] << shift)) {
						rot = matrix[0] << shift;
					}
			}
		}

		return out | rot;
	}

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
				if ((matrix[i] << shift) == rot) {
					rot = matrix[(i + 1) % matrix.length] << shift;
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

	public boolean isEqual(SchematicJSON s, int targetMeta, NBTTagCompound targetNBT) {
		if (type == BuilderRotation.Type.METADATA) {
			return (targetMeta & mask) == (s.meta & mask);
		} else if (type == BuilderRotation.Type.NBT_ROTATE_ARRAY) {
			// TODO
		} else if (type == BuilderRotation.Type.NBT_FIELD || type == Type.NBT_BITWISE_SHIFT) {
			NBTBase field1 = NBTUtils.getTag(s.tileNBT, tag);
			NBTBase field2 = NBTUtils.getTag(targetNBT, tag);
			if (field1 == null || field2 == null) {
				return false;
			} else {
				return (((NBTBase.NBTPrimitive) field1).func_150287_d() & mask) == (((NBTBase.NBTPrimitive) field1).func_150287_d() & mask);
			}
		}

		return true;
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
			NBTUtils.setInteger(s.tileNBT, tag, i);
		} else if (type == Type.NBT_BITWISE_SHIFT) {
			NBTBase field = NBTUtils.getTag(s.tileNBT, tag);
			if (field == null) {
				BCLog.logger.warn("Could not find field " + tag + " in tile " + s.tileNBT.getString("id") + "!");
				return;
			}
			int v = ((NBTBase.NBTPrimitive) field).func_150287_d();
			int bits = (v & mask) >> shift;
			int bitsNew = 0;
			for (int i = 0; i < transformation.length; i++) {
				bitsNew |= ((bits >> i) & 1) << transformation[i];
			}
			v = v & mask | (bitsNew << shift);
			NBTUtils.setInteger(s.tileNBT, tag, v);
		}
	}

	public void validate(BuilderSupportEntry e) throws JSONValidationException {
		if (!initialized && format != null) {
			if (format.equalsIgnoreCase("ForgeDirection") || format.equalsIgnoreCase("EnumFacing")) {
				format = "forgedirection";

				if (type == Type.NBT_ROTATE_ARRAY || type == Type.NBT_BITWISE_SHIFT) {
					transformation = new int[]{0, 1, 5, 4, 2, 3};
				} else {
					matrix = new int[]{2, 5, 3, 4};
				}

				if (mask == 0 && shift == 0) {
					mask = 7;
				}
			}
		}

		if (type == null || matrix == null) {
			throw new JSONValidationException(e, "Invalid rotation type!");
		}

		if (matrix == null && sticksToWall) {
			throw new JSONValidationException(e, "Must set rotation matrix (N->E->S->W) if block sticks to wall!");
		}

		if (shift < 0 || shift > 32) {
			throw new JSONValidationException(e, "Invalid shift value specified: " + shift + "!");
		}

		if (!initialized) {
			mask <<= shift;
		}

		if (type == Type.METADATA) {
			if (tag != null) {
				throw new JSONValidationException(e, "Must not specify tag when using s.metadata type!");
			}
			if ((mask & ~15) != 0) {
				throw new JSONValidationException(e, "Mask out of bounds!");
			}
		} else if (type == Type.NBT_FIELD || type == Type.NBT_ROTATE_ARRAY || type == Type.NBT_BITWISE_SHIFT) {
			if (tag == null) {
				throw new JSONValidationException(e, "Must specify tag when using NBT type!");
			}
			if (type == Type.NBT_ROTATE_ARRAY || type == Type.NBT_BITWISE_SHIFT) {
				if (transformation == null) {
					throw new JSONValidationException(e, "Must specify transformation when using NBT array rotation!");
				}
			}
		}

		if (sticksToWall) {
			if (matrix.length != 4) {
				throw new JSONValidationException(e, "SticksToWall only works for matrices of length 4 (N->E->S->W)!");
			} else if (type == Type.NBT_ROTATE_ARRAY || type == Type.NBT_BITWISE_SHIFT) {
				throw new JSONValidationException(e, "SticksToWall does not work for type " + type.name() + "!");
			}
		}

		initialized = true;
	}
}
