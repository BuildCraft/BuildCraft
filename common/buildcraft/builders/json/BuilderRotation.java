package buildcraft.builders.json;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.core.lib.utils.NBTUtils;
import net.minecraft.util.EnumFacing;

public class BuilderRotation {
	public boolean sticky;
	private String tag;
	private boolean invert = false;

	public EnumFacing getOrientation(SchematicJSON s) {
		String[] tagInfo = tag.split(":", 2);
		if ("state".equals(tagInfo[0])) {
			for (IProperty property : s.state.getPropertyNames()) {
				if (property.getName().equals(tagInfo[1]) && property instanceof PropertyDirection) {
					return (EnumFacing) s.state.getValue(property);
				}
			}
			return null;
		} else if ("nbt".equals(tagInfo[0])) {
			String[] nbtInfo = tagInfo[1].contains(":") ? tagInfo[1].split(":", 2) : new String[] {"facing", tagInfo[1]};
			NBTBase field = NBTUtils.getTag(s.tileNBT, nbtInfo[1]);
			if (field == null) {
				return null;
			}
			int i = ((NBTBase.NBTPrimitive) field).getInt();
			if ("facing".equals(tagInfo[0]) && i < 6) {
				return EnumFacing.getFront(i);
			} else if ("horizontal".equals(tagInfo[0])) {
				return EnumFacing.getHorizontal(i);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean isEqual(SchematicJSON s, IBlockState targetState, NBTTagCompound targetNBT) {
		String[] tagInfo = tag.split(":", 2);
		if ("state".equals(tagInfo[0])) {
			for (IProperty property : s.state.getPropertyNames()) {
				if (property.getName().equals(tagInfo[1]) && property instanceof PropertyDirection) {
					return s.state.getValue(property).equals(targetState.getValue(property));
				}
			}
		} else if ("nbt".equals(tagInfo[0])) {
			String[] nbtInfo = tagInfo[1].contains(":") ? tagInfo[1].split(":", 2) : new String[] {"facing", tagInfo[1]};
			NBTBase field1 = NBTUtils.getTag(s.tileNBT, nbtInfo[1]);
			NBTBase field2 = NBTUtils.getTag(targetNBT, nbtInfo[1]);
			if (field1 == null || field2 == null) {
				return field1 == field2;
			} else {
				return ((NBTBase.NBTPrimitive) field1).getInt() == ((NBTBase.NBTPrimitive) field2).getInt();
			}
		}

		return true;
	}

	public void rotateLeft(SchematicJSON s, IBuilderContext context) {
		EnumFacing rotated = getOrientation(s);
		if (rotated != null && rotated.getAxis() != EnumFacing.Axis.Y) {
			if (invert) {
				rotated.rotateYCCW();
			} else {
				rotated.rotateY();
			}
			String[] tagInfo = tag.split(":", 2);
			if ("state".equals(tagInfo[0])) {
				for (IProperty property : s.state.getPropertyNames()) {
					if (property.getName().equals(tagInfo[1]) && property instanceof PropertyDirection) {
						s.state = s.state.withProperty(property, rotated);
					}
				}
			} else if ("nbt".equals(tagInfo[0])) {
				String[] nbtInfo = tagInfo[1].contains(":") ? tagInfo[1].split(":", 2) : new String[]{"facing", tagInfo[1]};
				if ("facing".equals(tagInfo[0])) {
					NBTUtils.setInteger(s.tileNBT, nbtInfo[1], rotated.ordinal());
				} else if ("horizontal".equals(tagInfo[0])) {
					NBTUtils.setInteger(s.tileNBT, nbtInfo[1], rotated.getHorizontalIndex());
				}
			}
		}
	}

	public void validate(BuilderSupportEntry e) throws JSONValidationException {
		if (tag == null || !tag.contains(":")) {
			throw new JSONValidationException(e, "Invalid tag type!");
		}
	}
}
