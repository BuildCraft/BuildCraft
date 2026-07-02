package ct.buildcraft.api.enums;

import ct.buildcraft.api.core.IEngineType;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;

public enum EnumEngineType implements IEngineType, StringRepresentable, IExtensibleEnum {
    WOOD("buildcraftcore", "wood"),
    STONE("buildcraftenergy", "stone"),
    IRON("buildcraftenergy", "iron"),
    CREATIVE("buildcraftenergy", "creative");

    public final String unlocalizedTag;
    public final String resourceLocation;

    public static final EnumEngineType[] VALUES = values();

    EnumEngineType(String mod, String loc) {
        unlocalizedTag = loc;
        resourceLocation = mod + ":blocks/engine/inv/" + loc;
    }

    @Override
    public String getItemModelLocation() {
        return resourceLocation;
    }

	@Override
	public String getSerializedName() {
		return unlocalizedTag;
	}

    public static EnumEngineType fromMeta(int meta) {
        if (meta < 0 || meta >= VALUES.length) {
            meta = 0;
        }
        return VALUES[meta];
    }
    
    /*IExtensibleEnum*/
    public static EnumEngineType create(String name, String mod, String loc)
    {
        throw new IllegalStateException("Enum not extended");
    }


}
