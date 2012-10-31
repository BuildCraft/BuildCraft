package buildcraft.core;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.CreativeTabs;
import buildcraft.core.DefaultProps;

public class CreativeTabBC3 extends CreativeTabs
{

	CreativeTabBC3(String par1){
		super(par1);
	}
	@SideOnly(Side.CLIENT)
	public int getTabIconItemIndex(){
		return DefaultProps.ENGINE_ID; //Change NOW
	}

}