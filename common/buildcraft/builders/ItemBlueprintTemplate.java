/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumBlueprintType;
import buildcraft.builders.gui.GuiTemplate;
import buildcraft.core.blueprints.BlueprintBase;

public class ItemBlueprintTemplate extends ItemBlueprint {
    public ItemBlueprintTemplate() {
        super();
        setTextureLocation("template");
    }

    @Override
    public EnumBlueprintType getType(ItemStack stack) {
        return EnumBlueprintType.TEMPLATE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void openGui(BlueprintBase bpt) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiTemplate(bpt));
    }
}
