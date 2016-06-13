package buildcraft.builders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.builders.gui.ContainerBlueprintLibrary;
import buildcraft.builders.gui.GuiBlueprintLibrary;
import buildcraft.builders.tile.TileLibrary_Neptune;

public abstract class BuildersProxy_Neptune implements IGuiHandler {
    @SidedProxy
    private static BuildersProxy_Neptune proxy;

    public static BuildersProxy_Neptune getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == BuildersGuis.LIBRARY.ordinal()) {
            if (tile instanceof TileLibrary_Neptune) {
                TileLibrary_Neptune library = (TileLibrary_Neptune) tile;
                return new ContainerBlueprintLibrary(player, library);
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
    public static class ServerProxy extends BuildersProxy_Neptune {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BuildersProxy_Neptune {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

            if (ID == BuildersGuis.LIBRARY.ordinal()) {
                if (tile instanceof TileLibrary_Neptune) {
                    TileLibrary_Neptune library = (TileLibrary_Neptune) tile;
                    return new GuiBlueprintLibrary(player, library);
                }
            }

            return null;
        }
    }
}
