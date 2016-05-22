package buildcraft.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TransportProxy_BC8 implements IGuiHandler {
    @SidedProxy
    private static TransportProxy_BC8 proxy;

    public static TransportProxy_BC8 getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
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

    }
}
