/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.robotics.zone.ZonePlan;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ItemMapLocation extends ItemBC_Neptune implements IMapLocation {
    private static final String[] STORAGE_TAGS = "x,y,z,side,xMin,xMax,yMin,yMax,zMin,zMax,path,chunkMapping,name".split(",");

    public ItemMapLocation(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setHasSubtypes(true);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return MapLocationType.getFromStack(StackUtil.asNonNull(stack)) == MapLocationType.CLEAN ? 16 : 1;
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        for (MapLocationType type : MapLocationType.values()) {
//            addVariant(variants, type.meta, type.name().toLowerCase(Locale.ROOT));
//        }
//    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> strings, ITooltipFlag flag) {
        stack = StackUtil.asNonNull(stack);
        CompoundNBT cpt = NBTUtilBC.getItemData(stack);

        if (cpt.contains("name")) {
            String name = cpt.getString("name");
            if (name.length() > 0) {
                strings.add(new StringTextComponent(name));
            }
        }

        MapLocationType type = MapLocationType.getFromStack(stack);
        switch (type) {
            case SPOT: {
                if (cpt.contains("x") && cpt.contains("y") && cpt.contains("z") && cpt.contains("side")) {
                    int x = cpt.getInt("x");
                    int y = cpt.getInt("y");
                    int z = cpt.getInt("z");
                    Direction side = Direction.values()[cpt.getByte("side")];

                    strings.add(new StringTextComponent(LocaleUtil.localize("{" + x + ", " + y + ", " + z + ", " + side + "}")));
                }
                break;
            }
            case AREA: {
                if (cpt.contains("xMin") && cpt.contains("yMin") && cpt.contains("zMin") && cpt.contains("xMax")
                        && cpt.contains("yMax") && cpt.contains("zMax")) {
                    int x = cpt.getInt("xMin");
                    int y = cpt.getInt("yMin");
                    int z = cpt.getInt("zMin");
                    int xLength = cpt.getInt("xMax") - x + 1;
                    int yLength = cpt.getInt("yMax") - y + 1;
                    int zLength = cpt.getInt("zMax") - z + 1;

                    strings.add(new StringTextComponent(LocaleUtil.localize(
                            "{" + x + ", " + y + ", " + z + "} + {" + xLength + " x " + yLength + " x " + zLength + "}")));
                }
                break;
            }
            case PATH:
            case PATH_REPEATING: {
                if (cpt.contains("path")) {
                    ListNBT pathNBT = (ListNBT) cpt.get("path");

                    if (pathNBT.size() > 0) {
                        BlockPos first = NBTUtilBC.readBlockPos(pathNBT.get(0));
                        if (first != null) {
                            strings.add(new StringTextComponent("{" +
                                    StringUtilBC.blockPosToString(first) + "}, (+" + (pathNBT.size() - 1) + " elements)"));
                        }
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
        if (type != MapLocationType.CLEAN) {
//            strings.add(new StringTextComponent(LocaleUtil.localize("buildcraft.item.nonclean.usage")));
            strings.add(new TranslationTextComponent("buildcraft.item.nonclean.usage"));
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        if (player.isShiftKeyDown()) {
            return clearMarkerData(StackUtil.asNonNull(stack));
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private static ActionResult<ItemStack> clearMarkerData(@Nonnull ItemStack stack) {
        if (MapLocationType.getFromStack(stack) == MapLocationType.CLEAN) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        CompoundNBT nbt = NBTUtilBC.getItemData(stack);
        for (String key : STORAGE_TAGS) {
            nbt.remove(key);
        }
        if (nbt.isEmpty()) {
            stack.setTag(null);
        }
        MapLocationType.CLEAN.setToStack(stack);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
//    public ActionResultType onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand)
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        PlayerEntity player = context.getPlayer();
        if (world.isClientSide) {
            return ActionResultType.PASS;
        }

//        ItemStack stack = StackUtil.asNonNull(player.getHeldItem(hand));
        stack = StackUtil.asNonNull(stack);
        if (MapLocationType.getFromStack(stack) != MapLocationType.CLEAN) {
            return ActionResultType.FAIL;
        }

        ItemStack modified = stack;

        boolean anotherStack = false;
        if (stack.getCount() > 1) {
            modified = stack.copy();
//            stack.setCount(stack.getCount() - 1);
            stack.shrink(1);
            modified.setCount(1);
            anotherStack = true;
        }

        TileEntity tile = world.getBlockEntity(pos);
        CompoundNBT cpt = NBTUtilBC.getItemData(modified);

        if (tile instanceof IPathProvider) {
            List<BlockPos> path = ((IPathProvider) tile).getPath();

            if (path.size() > 1 && path.get(0).equals(path.get(path.size() - 1))) {
                MapLocationType.PATH_REPEATING.setToStack(stack);
            } else {
                MapLocationType.PATH.setToStack(stack);
            }

            ListNBT pathNBT = new ListNBT();

            for (BlockPos posInPath : path) {
                pathNBT.add(NBTUtilBC.writeBlockPos(posInPath));
            }

            cpt.put("path", pathNBT);
        } else if (tile instanceof IAreaProvider) {
            MapLocationType.AREA.setToStack(modified);

            IAreaProvider areaTile = (IAreaProvider) tile;

            cpt.putInt("xMin", areaTile.min().getX());
            cpt.putInt("yMin", areaTile.min().getY());
            cpt.putInt("zMin", areaTile.min().getZ());
            cpt.putInt("xMax", areaTile.max().getX());
            cpt.putInt("yMax", areaTile.max().getY());
            cpt.putInt("zMax", areaTile.max().getZ());

        } else {
            MapLocationType.SPOT.setToStack(modified);

            cpt.putByte("side", (byte) side.ordinal());
            cpt.putInt("x", pos.getX());
            cpt.putInt("y", pos.getY());
            cpt.putInt("z", pos.getZ());
        }

        // Calen FIXED: in 1.12.2 if map_location stack size > 1, the used one will not be given to player
        if (anotherStack) {
            player.inventory.add(modified);
        }

        return ActionResultType.SUCCESS;
    }

    public static IBox getAreaBox(@Nonnull ItemStack item) {
        CompoundNBT cpt = NBTUtilBC.getItemData(item);
        int xMin = cpt.getInt("xMin");
        int yMin = cpt.getInt("yMin");
        int zMin = cpt.getInt("zMin");
        BlockPos min = new BlockPos(xMin, yMin, zMin);

        int xMax = cpt.getInt("xMax");
        int yMax = cpt.getInt("yMax");
        int zMax = cpt.getInt("zMax");
        BlockPos max = new BlockPos(xMax, yMax, zMax);

        return new Box(min, max);
    }

    public static IBox getPointBox(@Nonnull ItemStack item) {
        CompoundNBT cpt = NBTUtilBC.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);

        switch (type) {
            case SPOT: {
                int x = cpt.getInt("x");
                int y = cpt.getInt("y");
                int z = cpt.getInt("z");

                BlockPos pos = new BlockPos(x, y, z);

                return new Box(pos, pos);
            }
            default: {
                return null;
            }
        }
    }

    public static Direction getPointFace(@Nonnull ItemStack stack) {
        CompoundNBT cpt = NBTUtilBC.getItemData(stack);
        return Direction.values()[cpt.getByte("side")];
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
    public Direction getPointSide(@Nonnull ItemStack item) {
        CompoundNBT cpt = NBTUtilBC.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);

        if (type == MapLocationType.SPOT) {
            return Direction.values()[cpt.getByte("side")];
        } else {
            return null;
        }
    }

    @Override
    public BlockPos getPoint(@Nonnull ItemStack item) {
        CompoundNBT cpt = NBTUtilBC.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);

        if (type == MapLocationType.SPOT) {
            return new BlockPos(cpt.getInt("x"), cpt.getInt("y"), cpt.getInt("z"));
        } else {
            return null;
        }
    }

    @Override
    public IZone getZone(@Nonnull ItemStack item) {
        CompoundNBT cpt = NBTUtilBC.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);
        switch (type) {
            case ZONE: {
                ZonePlan plan = new ZonePlan();
                plan.readFromNBT(cpt);
                return plan;
            }
            case SPOT:
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
        CompoundNBT cpt = NBTUtilBC.getItemData(item);
        MapLocationType type = MapLocationType.getFromStack(item);
        switch (type) {
            case PATH:
            case PATH_REPEATING: {
                List<BlockPos> indexList = new ArrayList<>();
                ListNBT pathNBT = (ListNBT) cpt.get("path");
                for (int i = 0; i < pathNBT.size(); i++) {
                    BlockPos pos = NBTUtilBC.readBlockPos(pathNBT.get(i));
                    if (pos != null) {
                        indexList.add(pos);
                    }
                }
                return indexList;
            }
            case SPOT: {
                List<BlockPos> indexList = new ArrayList<>();
                indexList.add(new BlockPos(cpt.getInt("x"), cpt.getInt("y"), cpt.getInt("z")));
                return indexList;
            }
            default: {
                return null;
            }
        }
    }

    public static void setZone(@Nonnull ItemStack item, ZonePlan plan) {
        CompoundNBT cpt = NBTUtilBC.getItemData(item);
        MapLocationType.ZONE.setToStack(item);
        plan.writeToNBT(cpt);
    }

    @Override
//    public String getName(@Nonnull ItemStack item)
    public String getName_INamedItem(@Nonnull ItemStack item) {
        return NBTUtilBC.getItemData(item).getString("name");
    }

    @Override
    public boolean setName(@Nonnull ItemStack item, String name) {
        CompoundNBT cpt = NBTUtilBC.getItemData(item);
        cpt.putString("name", name);
        return true;
    }
}
