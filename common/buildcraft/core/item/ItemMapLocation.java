/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.robotics.zone.ZonePlan;

public class ItemMapLocation extends ItemBC_Neptune implements IMapLocation {
    private static final String[] STORAGE_TAGS = "x,y,z,side,xMin,xMax,yMin,yMax,zMin,zMax,path,chunkMapping,name".split(",");

    public ItemMapLocation(String id) {
        super(id);
        setHasSubtypes(true);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return MapLocationType.getFromStack(StackUtil.asNonNull(stack)) == MapLocationType.CLEAN ? 16 : 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (MapLocationType type : MapLocationType.values()) {
            addVariant(variants, type.meta, type.name().toLowerCase(Locale.ROOT));
        }
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> strings, ITooltipFlag flag) {
        stack = StackUtil.asNonNull(stack);
        NBTTagCompound cpt = NBTUtilBC.getItemData(stack);

        if (cpt.hasKey("name")) {
            String name = cpt.getString("name");
            if (name.length() > 0) {
                strings.add(name);
            }
        }

        MapLocationType type = MapLocationType.getFromStack(stack);
        switch (type) {
            case SPOT: {
                if (cpt.hasKey("x") && cpt.hasKey("y") && cpt.hasKey("z") && cpt.hasKey("side")) {
                    int x = cpt.getInteger("x");
                    int y = cpt.getInteger("y");
                    int z = cpt.getInteger("z");
                    EnumFacing side = EnumFacing.VALUES[cpt.getByte("side")];

                    strings.add(LocaleUtil.localize("{" + x + ", " + y + ", " + z + ", " + side + "}"));
                }
                break;
            }
            case AREA: {
                if (cpt.hasKey("xMin") && cpt.hasKey("yMin") && cpt.hasKey("zMin") && cpt.hasKey("xMax") && cpt.hasKey("yMax") && cpt.hasKey("zMax")) {
                    int x = cpt.getInteger("xMin");
                    int y = cpt.getInteger("yMin");
                    int z = cpt.getInteger("zMin");
                    int xLength = cpt.getInteger("xMax") - x + 1;
                    int yLength = cpt.getInteger("yMax") - y + 1;
                    int zLength = cpt.getInteger("zMax") - z + 1;

                    strings.add(LocaleUtil.localize("{" + x + ", " + y + ", " + z + "} + {" + xLength + " x " + yLength + " x " + zLength + "}"));
                }
                break;
            }
            case PATH:
            case PATH_REPEATING: {
                if (cpt.hasKey("path")) {
                    NBTTagList pathNBT = (NBTTagList) cpt.getTag("path");

                    if (pathNBT.tagCount() > 0) {
                        BlockPos first = NBTUtilBC.readBlockPos(pathNBT.get(0));

                        int x = first.getX();
                        int y = first.getY();
                        int z = first.getZ();

                        strings.add(LocaleUtil.localize("{" + x + ", " + y + ", " + z + "} + " + (pathNBT.tagCount() - 1) + " elements"));
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
        if (type != MapLocationType.CLEAN) {
            strings.add(LocaleUtil.localize("buildcraft.item.nonclean.usage"));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (player.isSneaking()) {
            return clearMarkerData(StackUtil.asNonNull(stack));
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    private static ActionResult<ItemStack> clearMarkerData(@Nonnull ItemStack stack) {
        if (MapLocationType.getFromStack(stack) == MapLocationType.CLEAN) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        NBTTagCompound nbt = NBTUtilBC.getItemData(stack);
        for (String key : STORAGE_TAGS) {
            nbt.removeTag(key);
        }
        if (nbt.hasNoTags()) {
            stack.setTagCompound(null);
        }
        MapLocationType.CLEAN.setToStack(stack);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }

        ItemStack stack = StackUtil.asNonNull(player.getHeldItem(hand));
        if (MapLocationType.getFromStack(stack) != MapLocationType.CLEAN) {
            return EnumActionResult.FAIL;
        }

        ItemStack modified = stack;

        if (stack.getCount() > 1) {
            modified = stack.copy();
            stack.setCount(stack.getCount() - 1);
            modified.setCount(1);
        }

        TileEntity tile = world.getTileEntity(pos);
        NBTTagCompound cpt = NBTUtilBC.getItemData(modified);

        if (tile instanceof IPathProvider) {
            List<BlockPos> path = ((IPathProvider) tile).getPath();

            if (path.size() > 1 && path.get(0).equals(path.get(path.size() - 1))) {
                MapLocationType.PATH_REPEATING.setToStack(stack);
            } else {
                MapLocationType.PATH.setToStack(stack);
            }

            NBTTagList pathNBT = new NBTTagList();

            for (BlockPos posInPath : path) {
                pathNBT.appendTag(NBTUtilBC.writeBlockPos(posInPath));
            }

            cpt.setTag("path", pathNBT);
        } else if (tile instanceof IAreaProvider) {
            MapLocationType.AREA.setToStack(modified);

            IAreaProvider areaTile = (IAreaProvider) tile;

            cpt.setInteger("xMin", areaTile.min().getX());
            cpt.setInteger("yMin", areaTile.min().getY());
            cpt.setInteger("zMin", areaTile.min().getZ());
            cpt.setInteger("xMax", areaTile.max().getX());
            cpt.setInteger("yMax", areaTile.max().getY());
            cpt.setInteger("zMax", areaTile.max().getZ());

        } else {
            MapLocationType.SPOT.setToStack(modified);

            cpt.setByte("side", (byte) side.getIndex());
            cpt.setInteger("x", pos.getX());
            cpt.setInteger("y", pos.getY());
            cpt.setInteger("z", pos.getZ());
        }

        return EnumActionResult.SUCCESS;
    }

    public static IBox getAreaBox(@Nonnull ItemStack item) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(item);
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

    public static IBox getPointBox(@Nonnull ItemStack item) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(item);
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

    public static EnumFacing getPointFace(@Nonnull ItemStack stack) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(stack);
        return EnumFacing.VALUES[cpt.getByte("side")];
    }

    @Override
    public IBox getBox(@Nonnull ItemStack item) {
        MapLocationType type = MapLocationType.getFromStack(item);

        switch (type) {
            case AREA: {
                return getAreaBox(item);
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
    public EnumFacing getPointSide(@Nonnull ItemStack item) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);

        if (type == MapLocationType.SPOT) {
            return EnumFacing.VALUES[cpt.getByte("side")];
        } else {
            return null;
        }
    }

    @Override
    public BlockPos getPoint(@Nonnull ItemStack item) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);

        if (type == MapLocationType.SPOT) {
            return new BlockPos(cpt.getInteger("x"), cpt.getInteger("y"), cpt.getInteger("z"));
        } else {
            return null;
        }
    }

    @Override
    public IZone getZone(@Nonnull ItemStack item) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(item);
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
            case PATH:
            case PATH_REPEATING: {
                return getPointBox(item);
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public List<BlockPos> getPath(@Nonnull ItemStack item) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);
        switch (type) {
            case PATH:
            case PATH_REPEATING: {
                List<BlockPos> indexList = new ArrayList<>();
                NBTTagList pathNBT = (NBTTagList) cpt.getTag("path");
                for (int i = 0; i < pathNBT.tagCount(); i++) {
                    indexList.add(NBTUtilBC.readBlockPos(pathNBT.get(i)));
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

    public static void setZone(@Nonnull ItemStack item, ZonePlan plan) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(item);
        MapLocationType.ZONE.setToStack(item);
        plan.writeToNBT(cpt);
    }

    @Override
    public String getName(@Nonnull ItemStack item) {
        return NBTUtilBC.getItemData(item).getString("name");
    }

    @Override
    public boolean setName(@Nonnull ItemStack item, String name) {
        NBTTagCompound cpt = NBTUtilBC.getItemData(item);
        cpt.setString("name", name);
        return true;
    }
}
