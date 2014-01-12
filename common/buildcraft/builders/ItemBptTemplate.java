package buildcraft.builders;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class ItemBptTemplate extends ItemBptBase {
	private IIcon usedTemplate;
	public ItemBptTemplate(int i) {
		super(i);
	}

	@Override
	public IIcon getIconFromDamage(int i) {
		if (i == 0)
			return itemIcon;
		else
			return usedTemplate;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		itemIcon = par1IconRegister.registerIcon("buildcraft:template_clean");
		usedTemplate = par1IconRegister.registerIcon("buildcraft:template_used");
	}
}
