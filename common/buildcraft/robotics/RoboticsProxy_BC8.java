package buildcraft.robotics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.robotics.client.render.RenderZonePlanner;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.gui.GuiZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;

public abstract class RoboticsProxy_BC8 implements IGuiHandler {
    @SidedProxy
    private static RoboticsProxy_BC8 proxy;

    public static RoboticsProxy_BC8 getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == RoboticsGuis.ZONE_PLANTER.ordinal()) {
            if (tile instanceof TileZonePlanner) {
                TileZonePlanner zonePlanner = (TileZonePlanner) tile;
                return new ContainerZonePlanner(player, zonePlanner);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends RoboticsProxy_BC8 {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends RoboticsProxy_BC8 {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (ID == RoboticsGuis.ZONE_PLANTER.ordinal()) {
                if (tile instanceof TileZonePlanner) {
                    TileZonePlanner zonePlanner = (TileZonePlanner) tile;
                    return new GuiZonePlanner(new ContainerZonePlanner(player, zonePlanner));
                }
            }
            return null;
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            ClientRegistry.bindTileEntitySpecialRenderer(TileZonePlanner.class, new RenderZonePlanner());
        }
    }
}
