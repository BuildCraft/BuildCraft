/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
    private IItemLocalizationRule localizationRule = null;
    private String[] localizationRuleArray = null;

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

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        if (textureName == null) {
            ModelHelper.registerItemModel(this, 0, "");
        } else {
            ModelHelper.registerItemModel(this, 0, textureName, "");
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (localizationRule == null || localizationRuleArray == null) return super.getUnlocalizedName(stack);
        int index = localizationRule.getUnlocalizedNameFor(stack);
        if (index < 0 || index >= localizationRuleArray.length) return super.getUnlocalizedName(stack);
        return localizationRuleArray[index];
    }

    public void setLocalizationRuleArray(String... strings) {
        if (strings == null || strings.length == 0) throw new IllegalArgumentException("Not enough strings!");

        localizationRuleArray = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] == null || strings[i].length() == 0) throw new NullPointerException("strings[" + i + "]");
            localizationRuleArray[i] = strings[i];
        }
    }

    public void setLocalizationRule(IItemLocalizationRule rule) {
        if (localizationRuleArray == null) throw new IllegalStateException("Must set the array before setting the rule!");
        if (rule == null) throw new NullPointerException("rule");
        int forNull = rule.getUnlocalizedNameFor(null);
        if (forNull < 0 || forNull >= localizationRuleArray.length) throw new IllegalStateException("Must return a valid index for null!");
        localizationRule = rule;
    }

    public interface IItemLocalizationRule {
        int getUnlocalizedNameFor(ItemStack stack);
    }

}
