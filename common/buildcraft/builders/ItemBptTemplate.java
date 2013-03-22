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
			return iconIndex;
		else
			return usedTemplate;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateIcons(IconRegister par1IconRegister)
	{
	    super.updateIcons(par1IconRegister);
	    par1IconRegister.registerIcons("buildcraft:template_used");
	}
}
