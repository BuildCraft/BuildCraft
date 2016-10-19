package buildcraft.energy;

import buildcraft.lib.fluid.FluidManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BCEnergyProxy implements IGuiHandler {
    @SidedProxy
    private static BCEnergyProxy proxy;

    public static BCEnergyProxy getProxy() {
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

    public void fmlPreInit() {}

    public void fmlInit() {}

    public void fmlPostInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCEnergyProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCEnergyProxy {
        @Override
        public void fmlPreInit() {
            FluidManager.fmlPreInitClient();
        }

        @Override
        public void fmlInit() {
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            return null;
        }
    }
}
