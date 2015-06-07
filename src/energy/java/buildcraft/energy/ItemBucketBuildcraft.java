/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.BCCreativeTab;

public class ItemBucketBuildcraft extends ItemBucket {

    private String iconName;

    public ItemBucketBuildcraft(Block block) {
        this(block, BCCreativeTab.get("main"));
    }

    public ItemBucketBuildcraft(Block block, CreativeTabs creativeTab) {
        super(block);
        setContainerItem(Items.bucket);
        setCreativeTab(creativeTab);
    }

    @Override
    public Item setUnlocalizedName(String par1Str) {
        iconName = par1Str;
        return super.setUnlocalizedName(par1Str);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureAtlasSpriteRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon("buildcraftenergy:" + iconName);
    }
}
