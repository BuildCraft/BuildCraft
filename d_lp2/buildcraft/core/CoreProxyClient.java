package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import buildcraft.lib.guide.GuiGuide;

public class CoreProxyClient extends CoreProxy {
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == CoreGuis.GUIDE.ordinal()) {
            return new GuiGuide();
        }
        return null;
    }
}
