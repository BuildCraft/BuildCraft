package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.client.RenderTickListener;
import buildcraft.core.client.render.RenderMarkerPath;
import buildcraft.core.client.render.RenderMarkerVolume;
import buildcraft.core.list.GuiList;
import buildcraft.core.tile.TileMarkerPath;
import buildcraft.core.tile.TileMarkerVolume;
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
    public void fmlPreInit() {
        super.fmlPreInit();

        BuildCraftLaserManager.fmlPreInit();
    }

    @Override
    public void fmlInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMarkerVolume.class, RenderMarkerVolume.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileMarkerPath.class, RenderMarkerPath.INSTANCE);

        GuideManager guide = new GuideManager("buildcraftcore");
        GuideManager.registerManager(guide);
        guide.registerPage("item/gear_wood");
        guide.registerPage("item/gear_stone");
        guide.registerPage("item/gear_iron");
        guide.registerPage("item/gear_gold");
        guide.registerPage("item/gear_diamond");

        MinecraftForge.EVENT_BUS.register(RenderTickListener.INSTANCE);
    }
}
