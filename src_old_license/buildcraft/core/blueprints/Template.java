/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.blueprints;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.NBTUtils;

/** Use the template system to describe fillers */
public class Template extends BlueprintBase {

    public Template() {
        id.extension = "tpl";
    }

    public Template(BlockPos size) {
        super(size);
        id.extension = "tpl";
    }

    @Override
    public void readFromWorld(IBuilderContext context, TileEntity anchorTile, BlockPos pos) {
        Vec3d nPos = Utils.convert(pos).subtract(Utils.convert(context.surroundingBox().min()));

        if (!BuildCraftAPI.isSoftBlock(anchorTile.getWorld(), pos)) {
            set(Utils.convertFloor(nPos), new SchematicMask(true));
        }
    }

    @Override
    public void saveContents(NBTTagCompound nbt) {
        // Note: this way of storing data is suboptimal, we really need a bit
        // per mask entry, not a byte. However, this is fine, as compression
        // will fix it.

        byte[] data = new byte[size.getX() * size.getY() * size.getZ()];
        int ind = 0;

        for (int x = 0; x < size.getX(); ++x) {
            for (int y = 0; y < size.getY(); ++y) {
                for (int z = 0; z < size.getZ(); ++z) {
                    data[ind] = (byte) ((get(new BlockPos(x, y, z)) == null) ? 0 : 1);
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

        BCLog.logger.info("size = " + size);

        for (int x = 0; x < size.getX(); ++x) {
            for (int y = 0; y < size.getY(); ++y) {
                for (int z = 0; z < size.getZ(); ++z) {
                    if (data[ind] == 1) {
                        set(new BlockPos(x, y, z), new SchematicMask(true));
                    }
                    ind++;
                }
            }
        }
    }

    @Override
    public ItemStack getStack() {
        Item item = Item.REGISTRY.getObject(new ResourceLocation("BuildCraft|Builders:templateItem"));
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
