package buildcraft.lib.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;

public abstract class BlockBuildCraftTile_BC8 extends BlockBuildCraftBase_BC8 implements ITileEntityProvider {
    public BlockBuildCraftTile_BC8(Material material, String id) {
        super(material, id);
    }
}
