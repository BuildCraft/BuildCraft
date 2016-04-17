package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import buildcraft.lib.guide.GuiGuide;
import buildcraft.lib.guide.GuideManager;
import buildcraft.lib.item.ItemBuildCraft_BC8;

public class CoreProxyClient extends CoreProxy {
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == CoreGuis.GUIDE.ordinal()) {
            return new GuiGuide();
        }
        return null;
    }

    @Override
    public void fmlInit() {
        ItemBuildCraft_BC8.fmlInitClient();

        GuideManager guide = new GuideManager("buildcraftcore");
        GuideManager.registerManager(guide);
        guide.registerAllBlocks();
        guide.registerAllItems(false);
    }
}
