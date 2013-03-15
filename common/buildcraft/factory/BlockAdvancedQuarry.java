/**
 * Copyright (c) sadris, 2013
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockAdvancedQuarry extends BlockQuarry {

    public BlockAdvancedQuarry(int i) {
        super(i);

        super.textureSide = 4 * 16 + 12;
        super.textureFront = 4 * 16 + 10;
        super.textureTop = 4 * 16 + 11;

    }

    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileAdvancedQuarry();
    }
}
