/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.utils.IModelRegister;
import buildcraft.core.lib.utils.ModelHelper;

public class ItemBuildCraft extends Item implements IModelRegister {
    private boolean passSneakClick = false;
    protected String textureName = null;

    public ItemBuildCraft() {
        this(BCCreativeTab.get("main"));
    }

    public ItemBuildCraft(CreativeTabs creativeTab) {
        super();

        setCreativeTab(creativeTab);
    }

    /** Sets the custom name (can use slashes for folders) for the model location. */
    public ItemBuildCraft setTextureLocation(String name) {
        textureName = name;
        return this;
    }

    public Item setPassSneakClick(boolean passClick) {
        this.passSneakClick = passClick;
        return this;
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        return passSneakClick;
    }

    @SideOnly(Side.CLIENT)
    public void registerModels() {
        if (textureName == null) {
            ModelHelper.registerItemModel(this, 0, "");
        } else {
            ModelHelper.registerItemModel(this, 0, textureName, "");
        }
    }
}
