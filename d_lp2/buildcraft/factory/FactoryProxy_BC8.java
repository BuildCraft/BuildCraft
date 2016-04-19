package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class FactoryProxy_BC8 implements IGuiHandler {
    @SidedProxy(clientSide = "buildcraft.factory.FactoryProxyClient_BC8", serverSide = "buildcraft.factory.FactoryProxy_BC8")
    private static FactoryProxy_BC8 proxy;

    public static FactoryProxy_BC8 getProxy() {
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
}
