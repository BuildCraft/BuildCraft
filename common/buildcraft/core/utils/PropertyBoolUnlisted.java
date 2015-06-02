package buildcraft.core.utils;

import net.minecraft.block.properties.PropertyBool;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyBoolUnlisted extends PropertyBool implements IUnlistedProperty<Boolean> {
	public PropertyBoolUnlisted(String name) {
		super(name);
	}

	@Override
	public boolean isValid(Boolean value) {
		return value != null;
	}

	@Override
	public Class<Boolean> getType() {
		return Boolean.class;
	}

	@Override
	public String valueToString(Boolean value) {
		return value.toString();
	}
}
