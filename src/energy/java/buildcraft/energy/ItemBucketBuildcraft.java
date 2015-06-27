/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy;

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
        ModelHelper.registerItemModel(this, 0, "");
    }
}
