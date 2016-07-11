package buildcraft.transport;

import buildcraft.transport.container.ContainerFilteredBuffer;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.tile.TileFilteredBuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public abstract class TransportProxy_BC8 implements IGuiHandler {
    @SidedProxy
    private static TransportProxy_BC8 proxy;

    public static TransportProxy_BC8 getProxy() {
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

    public void fmlInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends TransportProxy_BC8 {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends TransportProxy_BC8 {
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
