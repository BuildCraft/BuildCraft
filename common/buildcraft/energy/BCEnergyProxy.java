package buildcraft.energy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.energy.client.gui.GuiEngineStone_BC8;
import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.energy.tile.TileEngineStone_BC8;

public abstract class BCEnergyProxy implements IGuiHandler {
    @SidedProxy
    private static BCEnergyProxy proxy;

    public static BCEnergyProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BCEnergyGuis gui = BCEnergyGuis.get(id);
        if (gui == null) return null;
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tile = world.getTileEntity(pos);
        switch (gui) {
            case ENGINE_STONE:
                if (tile instanceof TileEngineStone_BC8) {
                    return new ContainerEngineStone_BC8(player, (TileEngineStone_BC8) tile);
                }
                return null;
            case ENGINE_IRON:

                return null;
            default:
                return null;
        }
    }

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCEnergyProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCEnergyProxy {
        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            BCEnergyGuis gui = BCEnergyGuis.get(id);
            if (gui == null) return null;
            BlockPos pos = new BlockPos(x, y, z);
            TileEntity tile = world.getTileEntity(pos);
            switch (gui) {
                case ENGINE_STONE:
                    if (tile instanceof TileEngineStone_BC8) {
                        return new GuiEngineStone_BC8(new ContainerEngineStone_BC8(player, (TileEngineStone_BC8) tile));
                    }
                    return null;
                case ENGINE_IRON:

                    return null;
                default:
                    return null;
            }
        }
    }
}
