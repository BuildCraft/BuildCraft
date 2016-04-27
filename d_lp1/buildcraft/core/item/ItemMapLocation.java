/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;
import buildcraft.core.Box;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.lib.item.ItemBuildCraft_BC8;
import buildcraft.robotics.ZonePlan;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemMapLocation extends ItemBuildCraft_BC8 implements IMapLocation {
    public ItemMapLocation(String id) {
        super(id);
        setHasSubtypes(true);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return MapLocationType.getFromStack(stack) == MapLocationType.CLEAN ? 16 : 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (MapLocationType type : MapLocationType.values()) {
            addVariant(variants, type.meta, type.name().toLowerCase(Locale.ROOT));
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> strings, boolean advanced) {
        NBTTagCompound cpt = NBTUtils.getItemData(stack);

        if (cpt.hasKey("name")) {
            String name = cpt.getString("name");
            if (name.length() > 0) {
                strings.add(name);
            }
        }

        MapLocationType type = MapLocationType.getFromStack(stack);
        switch (type) {
            case SPOT: {
                int x = cpt.getInteger("x");
                int y = cpt.getInteger("y");
                int z = cpt.getInteger("z");
                EnumFacing side = EnumFacing.values()[cpt.getByte("side")];

                strings.add(BCStringUtils.localize("{" + x + ", " + y + ", " + z + ", " + side + "}"));
                break;
            }
            case AREA: {
                int x = cpt.getInteger("xMin");
                int y = cpt.getInteger("yMin");
                int z = cpt.getInteger("zMin");
                int xLength = cpt.getInteger("xMax") - x + 1;
                int yLength = cpt.getInteger("yMax") - y + 1;
                int zLength = cpt.getInteger("zMax") - z + 1;

                strings.add(BCStringUtils.localize("{" + x + ", " + y + ", " + z + "} + {" + xLength + " x " + yLength + " x " + zLength + "}"));
                break;
            }
            case PATH: {
                NBTTagList pathNBT = cpt.getTagList("path", Constants.NBT.TAG_COMPOUND);
                BlockPos first = NBTUtils.readBlockPos(pathNBT);

                int x = first.getX();
                int y = first.getY();
                int z = first.getZ();

                strings.add(BCStringUtils.localize("{" + x + ", " + y + ", " + z + "} + " + pathNBT.tagCount() + " elements"));
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }

        TileEntity tile = world.getTileEntity(pos);
        NBTTagCompound cpt = NBTUtils.getItemData(stack);

        if (tile instanceof IPathProvider) {
            MapLocationType.PATH.setToStack(stack);

            NBTTagList pathNBT = new NBTTagList();

            for (BlockPos index : ((IPathProvider) tile).getPath()) {
                pathNBT.appendTag(NBTUtils.writeBlockPos(index));
            }

            cpt.setTag("path", pathNBT);
        } else if (tile instanceof IAreaProvider) {
            MapLocationType.AREA.setToStack(stack);

            IAreaProvider areaTile = (IAreaProvider) tile;

            cpt.setInteger("xMin", areaTile.min().getX());
            cpt.setInteger("yMin", areaTile.min().getY());
            cpt.setInteger("zMin", areaTile.min().getZ());
            cpt.setInteger("xMax", areaTile.max().getX());
            cpt.setInteger("yMax", areaTile.max().getY());
            cpt.setInteger("zMax", areaTile.max().getZ());

        } else {
            MapLocationType.SPOT.setToStack(stack);

            cpt.setByte("side", (byte) side.getIndex());
            cpt.setInteger("x", pos.getX());
            cpt.setInteger("y", pos.getY());
            cpt.setInteger("z", pos.getZ());
        }

        return EnumActionResult.SUCCESS;
    }

    public static IBox getAreaBox(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        int xMin = cpt.getInteger("xMin");
        int yMin = cpt.getInteger("yMin");
        int zMin = cpt.getInteger("zMin");
        BlockPos min = new BlockPos(xMin, yMin, zMin);

        int xMax = cpt.getInteger("xMax");
        int yMax = cpt.getInteger("yMax");
        int zMax = cpt.getInteger("zMax");
        BlockPos max = new BlockPos(xMax, yMax, zMax);

        return new Box(min, max);
    }

    public static IBox getPointBox(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);

        switch (type) {
            case SPOT: {
                int x = cpt.getInteger("x");
                int y = cpt.getInteger("y");
                int z = cpt.getInteger("z");

                BlockPos pos = new BlockPos(x, y, z);

                return new Box(pos, pos);
            }
            default: {
                return null;
            }
        }
    }

    public static EnumFacing getPointFace(ItemStack stack) {
        NBTTagCompound cpt = NBTUtils.getItemData(stack);
        return EnumFacing.VALUES[cpt.getByte("side")];
    }

    @Override
    public IBox getBox(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);

        switch (type) {
            case AREA: {
                int xMin = cpt.getInteger("xMin");
                int yMin = cpt.getInteger("yMin");
                int zMin = cpt.getInteger("zMin");
                BlockPos min = new BlockPos(xMin, yMin, zMin);

                int xMax = cpt.getInteger("xMax");
                int yMax = cpt.getInteger("yMax");
                int zMax = cpt.getInteger("zMax");
                BlockPos max = new BlockPos(xMax, yMax, zMax);

                return new Box(min, max);
            }
            case SPOT: {
                return getPointBox(item);
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public EnumFacing getPointSide(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);

        if (type == MapLocationType.SPOT) {
            return EnumFacing.values()[cpt.getByte("side")];
        } else {
            return null;
        }
    }

    @Override
    public BlockPos getPoint(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);

        if (type == MapLocationType.SPOT) {
            return new BlockPos(cpt.getInteger("x"), cpt.getInteger("y"), cpt.getInteger("z"));
        } else {
            return null;
        }
    }

    @Override
    public IZone getZone(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);
        switch (type) {
            case ZONE: {
                ZonePlan plan = new ZonePlan();
                plan.readFromNBT(cpt);
                return plan;
            }
            case AREA: {
                return getBox(item);
            }
            case PATH: {
                return getPointBox(item);
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public List<BlockPos> getPath(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);
        switch (type) {
            case PATH: {
                List<BlockPos> indexList = new ArrayList<>();
                NBTTagList pathNBT = cpt.getTagList("path", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < pathNBT.tagCount(); i++) {
                    indexList.add(NBTUtils.readBlockPos(pathNBT.getCompoundTagAt(i)));
                }
                return indexList;
            }
            case SPOT: {
                List<BlockPos> indexList = new ArrayList<>();
                indexList.add(new BlockPos(cpt.getInteger("x"), cpt.getInteger("y"), cpt.getInteger("z")));
                return indexList;
            }
            default: {
                return null;
            }
        }
    }

    public static void setZone(ItemStack item, ZonePlan plan) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        MapLocationType.ZONE.setToStack(item);
        plan.writeToNBT(cpt);
    }

    @Override
    public String getName(ItemStack item) {
        return NBTUtils.getItemData(item).getString("name");
    }

    @Override
    public boolean setName(ItemStack item, String name) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        cpt.setString("name", name);
        return true;
    }
}
