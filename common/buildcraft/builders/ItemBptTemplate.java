package buildcraft.builders;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBptTemplate extends ItemBptBase {
	private Icon usedTemplate;
	public ItemBptTemplate(int i) {
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
	public void registerIcons(IconRegister par1IconRegister)
	{
		itemIcon = par1IconRegister.registerIcon("buildcraft:template_clean");
		usedTemplate = par1IconRegister.registerIcon("buildcraft:template_used");
	}
}
