/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import buildcraft.silicon.gui.*;

public class SiliconGuiHandler implements IGuiHandler {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);

        if (!world.isBlockLoaded(pos)) {
            return null;
        }

        TileEntity tile = world.getTileEntity(pos);

        switch (id) {

            case 0:
                if (!(tile instanceof TileAssemblyTable)) {
                    return null;
                } else {
                    return new GuiAssemblyTable(player, (TileAssemblyTable) tile);
                }

            case 1:
                if (!(tile instanceof TileAdvancedCraftingTable)) {
                    return null;
                } else {
                    return new GuiAdvancedCraftingTable(player, (TileAdvancedCraftingTable) tile);
                }

            case 2:
                if (!(tile instanceof TileIntegrationTable)) {
                    return null;
                } else {
                    return new GuiIntegrationTable(player, (TileIntegrationTable) tile);
                }

            case 3:
                if (!(tile instanceof TileChargingTable)) {
                    return null;
                } else {
                    return new GuiChargingTable(player, (TileChargingTable) tile);
                }

            case 4:
                if (!(tile instanceof TileProgrammingTable)) {
                    return null;
                } else {
                    return new GuiProgrammingTable(player, (TileProgrammingTable) tile);
                }

            case 5:
                if (!(tile instanceof TileStampingTable)) {
                    return null;
                } else {
                    return new GuiStampingTable(player, (TileStampingTable) tile);
                }

            case 10:
                if (!(tile instanceof TilePackager)) {
                    return null;
                } else {
                    return new GuiPackager(player, (TilePackager) tile);
                }

            default:
                return null;
        }
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);

        if (!world.isBlockLoaded(pos)) {
            return null;
        }

        TileEntity tile = world.getTileEntity(pos);

        switch (id) {

            case 0:
                if (!(tile instanceof TileAssemblyTable)) {
                    return null;
                } else {
                    return new ContainerAssemblyTable(player, (TileAssemblyTable) tile);
                }

            case 1:
                if (!(tile instanceof TileAdvancedCraftingTable)) {
                    return null;
                } else {
                    return new ContainerAdvancedCraftingTable(player, (TileAdvancedCraftingTable) tile);
                }

            case 2:
                if (!(tile instanceof TileIntegrationTable)) {
                    return null;
                } else {
                    return new ContainerIntegrationTable(player, (TileIntegrationTable) tile);
                }

            case 3:
                if (!(tile instanceof TileChargingTable)) {
                    return null;
                } else {
                    return new ContainerChargingTable(player, (TileChargingTable) tile);
                }

            case 4:
                if (!(tile instanceof TileProgrammingTable)) {
                    return null;
                } else {
                    return new ContainerProgrammingTable(player, (TileProgrammingTable) tile);
                }

            case 5:
                if (!(tile instanceof TileStampingTable)) {
                    return null;
                } else {
                    return new ContainerStampingTable(player, (TileStampingTable) tile);
                }

            case 10:
                if (!(tile instanceof TilePackager)) {
                    return null;
                } else {
                    return new ContainerPackager(player, (TilePackager) tile);
                }

            default:
                return null;
        }
    }
}
