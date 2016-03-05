/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.fluids;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;

import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.utils.IModelRegister;
import buildcraft.core.lib.utils.ModelHelper;

public class ItemBucketBuildcraft extends ItemBucket implements IModelRegister {
    public ItemBucketBuildcraft(Block block) {
        this(block, BCCreativeTab.get("main"));
    }

    public ItemBucketBuildcraft(Block block, CreativeTabs creativeTab) {
        super(block);
        setContainerItem(Items.bucket);
        setCreativeTab(creativeTab);
    }

    @Override
    public void registerModels() {
        ModelHelper.registerItemModel(this, 0, "forge:dynbucket", "");
    }

    // @Override
    // public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
    // tooltip.add("Deprecated");
    // tooltip.add("Place and pick up for");
    // tooltip.add("the new version");
    // }
}
