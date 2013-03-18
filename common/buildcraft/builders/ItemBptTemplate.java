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
	public void func_94581_a(IconRegister par1IconRegister)
	{
	    super.func_94581_a(par1IconRegister);
	    par1IconRegister.func_94245_a("buildcraft:template_used");
	}
}
