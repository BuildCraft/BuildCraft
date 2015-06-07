/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Loader;

import buildcraft.api.transport.IInjectable;

public class CompatHooks {
    public static final CompatHooks INSTANCE;

    static {
        CompatHooks i = null;
        if (Loader.isModLoaded("BuildCraft|Compat")) {
            try {
                i = (CompatHooks) CompatHooks.class.getClassLoader().loadClass("buildcraft.compat.CompatHooksImpl").newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (i == null) {
            i = new CompatHooks();
        }

        INSTANCE = i;
    }

    public CompatHooks() {

    }

    public IInjectable getInjectableWrapper(TileEntity tile, EnumFacing side) {
        return null;
    }

    public Block getVisualBlock(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return null;
    }

    public int getVisualMeta(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return -1;
    }

    public Block getBlock(Class<? extends Block> klazz) {
        Block block = null;

        if (Loader.isModLoaded("BuildCraft|Compat")) {
            try {
                block = (Block) CompatHooks.class.getClassLoader().loadClass(klazz.getName() + "Compat").newInstance();
            } catch (ClassNotFoundException e) {
                // Class not supplied by Compat
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (block == null) {
            try {
                block = klazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return block;
    }

    public Class<? extends TileEntity> getTile(Class<? extends TileEntity> klazz) {
        Class<? extends TileEntity> tileClass = klazz;

        if (Loader.isModLoaded("BuildCraft|Compat")) {
            try {
                tileClass = (Class<? extends TileEntity>) CompatHooks.class.getClassLoader().loadClass(klazz.getName() + "Compat");
            } catch (ClassNotFoundException e) {
                // Class not supplied by Compat
                tileClass = klazz;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return tileClass;
    }

    public Object getEnergyProvider(TileEntity tile) {
        return tile;
    }
}
