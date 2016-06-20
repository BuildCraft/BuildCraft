package buildcraft.core.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.item.ItemBlockBCMulti;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemBlockDecorated extends ItemBlockBCMulti {

    public ItemBlockDecorated(BlockBCBase_Neptune block) {
        super(block, createNameArray());
    }

    private static String[] createNameArray() {
        String[] names = new String[EnumDecoratedBlock.VALUES.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = EnumDecoratedBlock.VALUES[i].getName();
        }
        return names;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (EnumDecoratedBlock type : EnumDecoratedBlock.VALUES) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }
}
