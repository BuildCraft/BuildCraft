/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.fluids;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.block.BlockBuildCraftFluid;
import buildcraft.core.lib.fluids.FluidDefinition.BCFluid;
import buildcraft.core.lib.utils.IModelRegister;
import buildcraft.core.lib.utils.ModelHelper;

public class ItemBucketBuildcraft extends ItemBucket implements IModelRegister {
    private final BCFluid fluid;

    public ItemBucketBuildcraft(BlockBuildCraftFluid block, BCFluid fluid) {
        this(block, fluid, BCCreativeTab.get("main"));
    }

    public ItemBucketBuildcraft(Block block, BCFluid fluid, CreativeTabs creativeTab) {
        super(block);
        this.fluid = fluid;
        setContainerItem(Items.bucket);
        setCreativeTab(creativeTab);
    }

    @Override
    public void registerModels() {
        ModelHelper.registerItemModel(this, 0, "forge:dynbucket", "");
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String unloc = I18n.translateToLocal(fluid.getUnlocalizedName());
        String s = "buildcraft.fluid.heat_" + fluid.getHeatValue();
        String heatString = I18n.translateToLocal(s);
        if (s.equals(heatString) || !fluid.isHeatable()) heatString = "";
        return unloc + " " + I18n.translateToLocal("item.bucket.name") + heatString;
    }
}
