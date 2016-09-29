package buildcraft.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.transport.container.ContainerFilteredBuffer;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.tile.TileFilteredBuffer;

public abstract class BCTransportProxy implements IGuiHandler {
    @SidedProxy
    private static BCTransportProxy proxy;

    public static BCTransportProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == TransportGuis.FILTERED_BUFFER.ordinal()) {
            if (tile instanceof TileFilteredBuffer) {
                TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                return new ContainerFilteredBuffer(player, filteredBuffer);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {}

    public void fmlInit() {}

    public void fmlPostInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCTransportProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCTransportProxy {
        @Override
        public void fmlPreInit() {
            BCTransportSprites.fmlPreInit();
            BCTransportModels.fmlPreInit();
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (ID == TransportGuis.FILTERED_BUFFER.ordinal()) {
                if (tile instanceof TileFilteredBuffer) {
                    TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                    return new GuiFilteredBuffer(new ContainerFilteredBuffer(player, filteredBuffer));
                }
            }
            return null;
        }
    }
}
