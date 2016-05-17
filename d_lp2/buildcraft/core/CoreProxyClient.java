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
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.render.DetatchedRenderer;
import buildcraft.lib.client.render.DetatchedRenderer.RenderMatrixType;

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

        // DetatchedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, RenderMarkerVolume.INSTANCE);
        DetatchedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, RenderMarkerPath.INSTANCE);
    }

    @Override
    public void fmlInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMarkerVolume.class, RenderMarkerVolume.INSTANCE);
        // ClientRegistry.bindTileEntitySpecialRenderer(TileMarkerPath.class, RenderMarkerPath.INSTANCE);

        MinecraftForge.EVENT_BUS.register(RenderTickListener.INSTANCE);
    }

    @Override
    public void fmlPostInit() {
        GuideManager guide = new GuideManager("buildcraftcore");
        GuideManager.registerManager(guide);
        guide.registerPage("item/gear_wood.md");
        guide.registerPage("item/gear_stone.md");
        guide.registerPage("item/gear_iron.md");
        guide.registerPage("item/gear_gold.md");
        guide.registerPage("item/gear_diamond.md");
        guide.registerPage("item/wrench.md");
        guide.registerPage("item/paintbrush.md");
        guide.registerPage("block/marker_volume.md");
        guide.registerPage("block/marker_path.md");
        guide.registerPage("item/marker_connector.md");
    }
}
