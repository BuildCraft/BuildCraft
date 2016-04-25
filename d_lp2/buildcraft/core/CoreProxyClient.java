package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;

import buildcraft.core.client.RenderTickListener;
import buildcraft.core.list.GuiList;
import buildcraft.lib.guide.GuiGuide;
import buildcraft.lib.guide.GuideManager;

public class CoreProxyClient extends CoreProxy {
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == CoreGuis.GUIDE.ordinal()) {
            return new GuiGuide();
        } else if (ID == CoreGuis.LIST.ordinal()) {
            return new GuiList(player);
        }
        return null;
    }

    @Override
    public void fmlInit() {
        GuideManager guide = new GuideManager("buildcraftcore");
        GuideManager.registerManager(guide);
        guide.registerAllBlocks();
        guide.registerAllItems(false);

        MinecraftForge.EVENT_BUS.register(RenderTickListener.INSTANCE);
    }
}
