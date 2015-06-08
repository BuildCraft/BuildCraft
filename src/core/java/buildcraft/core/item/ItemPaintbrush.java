/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blocks.IColorRemovable;
import buildcraft.api.enums.EnumColor;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.core.lib.utils.NBTUtils;

public class ItemPaintbrush extends ItemBuildCraft {
    private static final int MAX_DAMAGE = 63;

    public ItemPaintbrush() {
        super();
        setFull3D();
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(MAX_DAMAGE);
    }

    /** @param color The colour of the paintbrush. Can be null. */
    public ItemStack getItemStack(EnumColor color) {
        ItemStack stack = new ItemStack(this, 1, color == null ? 0 : color.ordinal() + 1);
        NBTTagCompound nbt = NBTUtils.getItemData(stack);

        // Tell ItemModelMesher that this is NOT damageable, so it will use the meta for the icon
        nbt.setBoolean("Unbreakable", true);

        // Tell ItemStack.getToolTip() that we want to hide the resulting "Unbreakable" line that we just added
        nbt.setInteger("HideFlags", 4);

        return stack;

    }

    private int getColor(ItemStack stack) {
        int meta = stack.getMetadata();
        if (meta < 1 || meta > 16) {
            return -1;
        } else {
            return meta - 1;
        }
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        if (damage > MAX_DAMAGE) {
            stack.setTagCompound(null);
            super.setDamage(stack, 0);
        } else {
            NBTTagCompound tag = NBTUtils.getItemData(stack);
            tag.setByte("damage", (byte) damage);
        }
    }

    @Override
    public int getDamage(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return 0;
        } else {
            return nbt.getByte("damage");
        }
    }

    @Override
    public int getMetadata(ItemStack stack) {
        return super.getDamage(stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String base = super.getItemStackDisplayName(stack);
        int dye = getColor(stack);
        if (dye >= 0) {
            return base + " (" + EnumColor.fromId(dye).getLocalizedName() + ")";
        } else {
            return base;
        }
    }

    @Override
    public boolean
            onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            // return true;
        }
        int dye = getColor(stack);
        Block block = world.getBlockState(pos).getBlock();

        if (block == null) {
            return true;
        }

        if (dye >= 0) {
            if (block.recolorBlock(world, pos, side, EnumDyeColor.byMetadata(15 - dye))) {
                player.swingItem();
                setDamage(stack, getDamage(stack) + 1);
                return true;
            }
        } else {
            // NOTE: Clean paint brushes never damage.
            if (block instanceof IColorRemovable) {
                if (((IColorRemovable) block).removeColorFromBlock(world, pos, side)) {
                    player.swingItem();
                    return !world.isRemote;
                }
            }
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List itemList) {
        for (int i = 0; i < 17; i++) {
            EnumColor color = i == 0 ? null : EnumColor.VALUES[i - 1];
            itemList.add(this.getItemStack(color));
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return false;
        }
        return nbt.getByte("damage") > 0;
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public void registerModels() {
        ModelHelper.registerItemModel(this, 0, "/Clean");
        int i = 1;
        for (EnumColor colour : EnumColor.values()) {
            ModelHelper.registerItemModel(this, i, "/" + colour.getName());
            i++;
        }
    }
}
