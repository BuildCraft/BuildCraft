package buildcraft.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;
import buildcraft.lib.engine.BlockEngineBase_BC8;

public class BlockEngine_BC8 extends BlockEngineBase_BC8<EnumEngineType> {
    public BlockEngine_BC8(Material material, String id) {
        super(material, id);
    }

    @Override
    public IProperty<EnumEngineType> getEngineProperty() {
        return BuildCraftProperties.ENGINE_TYPE;
    }

    @Override
    protected EnumEngineType getEngineType(int meta) {
        return EnumEngineType.fromMeta(meta);
    }

    @Override
    public String getUnlocalizedName(EnumEngineType engine) {
        return TagManager.getTag("block.engine.bc." + engine.unlocalizedTag, EnumTagType.UNLOCALIZED_NAME);
    }
}
