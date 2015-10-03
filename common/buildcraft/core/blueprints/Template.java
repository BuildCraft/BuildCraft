/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.blueprints;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;

/** Use the template system to describe fillers */
public class Template extends BlueprintBase {

    public Template() {
        id.extension = "tpl";
    }

    public Template(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);

        id.extension = "tpl";
    }

    @Override
    public void readFromWorld(IBuilderContext context, TileEntity anchorTile, BlockPos pos) {
        Vec3 nPos = Utils.convert(pos).subtract(context.surroundingBox().pMin());
        int posX = (int) nPos.xCoord;
        int posY = (int) nPos.yCoord;
        int posZ = (int) nPos.zCoord;

        if (!BuildCraftAPI.isSoftBlock(anchorTile.getWorld(), pos)) {
            contents[posX][posY][posZ] = new SchematicMask(true);
        }
    }

    @Override
    public void saveContents(NBTTagCompound nbt) {
        // Note: this way of storing data is suboptimal, we really need a bit
        // per mask entry, not a byte. However, this is fine, as compression
        // will fix it.

        byte[] data = new byte[sizeX * sizeY * sizeZ];
        int ind = 0;

        for (int x = 0; x < sizeX; ++x) {
            for (int y = 0; y < sizeY; ++y) {
                for (int z = 0; z < sizeZ; ++z) {
                    data[ind] = (byte) ((contents[x][y][z] == null) ? 0 : 1);
                    ind++;
                }
            }
        }

        nbt.setByteArray("mask", data);
    }

    @Override
    public void loadContents(NBTTagCompound nbt) throws BptError {
        byte[] data = nbt.getByteArray("mask");
        int ind = 0;

        for (int x = 0; x < sizeX; ++x) {
            for (int y = 0; y < sizeY; ++y) {
                for (int z = 0; z < sizeZ; ++z) {
                    if (data[ind] == 1) {
                        contents[x][y][z] = new SchematicMask(true);
                    }

                    ind++;
                }
            }
        }
    }

    @Override
    public ItemStack getStack() {
        Item item = (Item) Item.itemRegistry.getObject("BuildCraft|Builders:templateItem");
        if (item == null) {
            throw new Error("Could not find the template item! Did you attempt to use this without buildcraft builders installed?");
        }
        ItemStack stack = new ItemStack(item, 1, 1);
        NBTTagCompound nbt = NBTUtils.getItemData(stack);
        id.write(nbt);
        nbt.setString("author", author);
        nbt.setString("name", id.name);

        return stack;
    }

}
