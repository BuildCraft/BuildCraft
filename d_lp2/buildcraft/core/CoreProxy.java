package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CoreProxy implements IGuiHandler {
    @SidedProxy(serverSide = "buildcraft.core.CoreProxy", clientSide = "buildcraft.core.CoreProxyClient")
    private static CoreProxy proxy = null;

    public static CoreProxy getProxy() {
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
}
