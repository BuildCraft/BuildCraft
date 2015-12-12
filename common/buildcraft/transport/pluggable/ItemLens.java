/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pluggable;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableItem;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.BCStringUtils;

public class ItemLens extends ItemBuildCraft implements IPipePluggableItem {
    public ItemLens() {
        super();
        setHasSubtypes(true);
    }

    public int getDye(ItemStack stack) {
        return 15 - (stack.getItemDamage() & 15);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        if (stack.getItemDamage() >= 32) {
            return 16777215;
        }
        return pass == 0 ? ColorUtils.getRGBColor(getDye(stack)) : 16777215;
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemstack) {
        if (itemstack.getItemDamage() >= 32) {
            return BCStringUtils.localize(itemstack.getItemDamage() == 33 ? "item.Filter.name" : "item.Lens.name") + " (" + BCStringUtils.localize(
                    "color.clear") + ")";
        } else {
            return BCStringUtils.localize(itemstack.getItemDamage() >= 16 ? "item.Filter.name" : "item.Lens.name") + " (" + BCStringUtils.localize(
                    "color." + ColorUtils.getName(getDye(itemstack))) + ")";
        }
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List itemList) {
        for (int i = 0; i < 34; i++) {
            itemList.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public PipePluggable createPipePluggable(IPipe pipe, EnumFacing side, ItemStack stack) {
        if (pipe.getTile().getPipeType() == IPipeTile.PipeType.ITEM) {
            return new LensPluggable(stack);
        } else {
            return null;
        }
    }
}
