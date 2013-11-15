package buildcraft.builders;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class ItemBlueprintTemplate extends ItemBlueprint {

	private Icon usedTemplate;

	public ItemBlueprintTemplate(int i) {
		super(i);
	}

	@Override
	public Icon getIconFromDamage(int i) {
		if (i == 0)
			return itemIcon;
		else
			return usedTemplate;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		itemIcon = par1IconRegister.registerIcon("buildcraft:template_clean");
		usedTemplate = par1IconRegister.registerIcon("buildcraft:template_used");
	}
}
