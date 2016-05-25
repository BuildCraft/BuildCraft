/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.robotics.ZonePlan;

public class ItemMapLocation extends ItemBuildCraft implements IMapLocation {
    public ItemMapLocation() {
        super(BCCreativeTab.get("main"));
        setHasSubtypes(true);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return MapLocationType.getFromStack(stack) == MapLocationType.CLEAN ? 16 : 1;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, @SuppressWarnings("rawtypes") List list, boolean advanced) {
        @SuppressWarnings("unchecked")
        List<String> strings = list;
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(stack);
        if (cpt == null)
            return;
        
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
                NBTTagList pathNBT = cpt.getTagList("path", NBTUtils.DEFAULT_BLOCK_POS_TAG);
                BlockPos first = NBTUtils.readBlockPos(pathNBT.get(0));

                int x = first.getX();
                int y = first.getY();
                int z = first.getZ();

                strings.add(BCStringUtils.localize("{" + x + ", " + y + ", " + z + "} + " + (pathNBT.tagCount()-1) + " elements"));
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (MapLocationType.getFromStack(stack) == MapLocationType.CLEAN) {
            MovingObjectPosition movingobjectposition = getMovingObjectPositionFromPlayer(world, player);
            if (movingobjectposition != null
                    && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos pos = movingobjectposition.getBlockPos();
                ItemStack toChange = stack.stackSize > 1 ? stack.splitStack(1) : stack;
                TileEntity tile = world.getTileEntity(pos);
                NBTTagCompound cpt = NBTUtils.getItemData(toChange);

                if (tile instanceof IPathProvider) {
                    MapLocationType.PATH.setToStack(toChange);

                    NBTTagList pathNBT = new NBTTagList();

                    for (BlockPos index : ((IPathProvider) tile).getPath()) {
                        pathNBT.appendTag(NBTUtils.writeBlockPos(index));
                    }

                    cpt.setTag("path", pathNBT);
                } else if (tile instanceof IAreaProvider) {
                    MapLocationType.AREA.setToStack(toChange);

                    IAreaProvider areaTile = (IAreaProvider) tile;

                    cpt.setInteger("xMin", areaTile.min().getX());
                    cpt.setInteger("yMin", areaTile.min().getY());
                    cpt.setInteger("zMin", areaTile.min().getZ());
                    cpt.setInteger("xMax", areaTile.max().getX());
                    cpt.setInteger("yMax", areaTile.max().getY());
                    cpt.setInteger("zMax", areaTile.max().getZ());

                } else {
                    MapLocationType.SPOT.setToStack(toChange);

                    cpt.setByte("side", (byte) movingobjectposition.sideHit.getIndex());
                    cpt.setInteger("x", pos.getX());
                    cpt.setInteger("y", pos.getY());
                    cpt.setInteger("z", pos.getZ());
                }
                if (toChange != stack && !player.inventory.addItemStackToInventory(toChange)) {
                    player.dropItem(toChange, false, true);
                }
            }
        } else if (player.isSneaking()) {
            clearMapLocation(stack);
        }
        return stack;
    }

    protected MovingObjectPosition getMovingObjectPositionFromPlayer(World worldIn, EntityPlayer playerIn) {
        float f = playerIn.rotationPitch;
        float f1 = playerIn.rotationYaw;
        double d0 = playerIn.posX;
        double d1 = playerIn.posY + (double) playerIn.getEyeHeight();
        double d2 = playerIn.posZ;
        Vec3 vec3 = new Vec3(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d3 = 5.0D;
        if (playerIn instanceof net.minecraft.entity.player.EntityPlayerMP) {
            d3 = ((net.minecraft.entity.player.EntityPlayerMP) playerIn).theItemInWorldManager.getBlockReachDistance();
        }
        Vec3 vec31 = vec3.addVector((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
        return worldIn.rayTraceBlocks(vec3, vec31, false, false, false);
    }

    private static void clearMapLocation(ItemStack stack) {
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(stack);
        // Remove all the tags and not the whole tag compound to allow
        // enchantments and other things in the tag compound line display names
        if (cpt != null) {
            cpt.removeTag("xMin");
            cpt.removeTag("yMin");
            cpt.removeTag("zMin");
            cpt.removeTag("xMax");
            cpt.removeTag("yMax");
            cpt.removeTag("zMax");
            cpt.removeTag("path");
            cpt.removeTag("side");
            cpt.removeTag("x");
            cpt.removeTag("y");
            cpt.removeTag("z");
            cpt.removeTag("chunkMapping");
            cpt.removeTag("name");
            if (cpt.hasNoTags()) {
                stack.setTagCompound(null);
            }
        }
        MapLocationType.CLEAN.setToStack(stack);
    }
    
    @Override
    public IBox getBox(ItemStack item) {
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

    public static IBox getAreaBox(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(item);
        if (cpt == null || MapLocationType.getFromStack(item) != MapLocationType.AREA)
            return null;
        
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
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(item);
        if (cpt == null || MapLocationType.getFromStack(item) != MapLocationType.SPOT)
            return null;
       
        int x = cpt.getInteger("x");
        int y = cpt.getInteger("y");
        int z = cpt.getInteger("z");

        BlockPos pos = new BlockPos(x, y, z);

        return new Box(pos, pos);
    }

    public static EnumFacing getPointFace(ItemStack stack) {
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(stack);
        if (cpt == null || MapLocationType.getFromStack(stack) != MapLocationType.SPOT)
            return null;
        
        return EnumFacing.VALUES[cpt.getByte("side")];
    }

    @Override
    public EnumFacing getPointSide(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(item);
        if (cpt == null || MapLocationType.getFromStack(item) != MapLocationType.SPOT)
            return null;

        return EnumFacing.values()[cpt.getByte("side")];
    }

    @Override
    public BlockPos getPoint(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(item);
        if (cpt == null || MapLocationType.getFromStack(item) != MapLocationType.SPOT)
            return null;

        return new BlockPos(cpt.getInteger("x"), cpt.getInteger("y"), cpt.getInteger("z"));
    }

    @Override
    public IZone getZone(ItemStack item) {
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(item);
        if (cpt == null)
            return null;
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
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(item);
        if (cpt == null)
            return null;
        MapLocationType type = MapLocationType.getFromStack(item);
        switch (type) {
            case PATH: {
                List<BlockPos> indexList = new ArrayList<>();
                NBTTagList pathNBT = cpt.getTagList("path", NBTUtils.DEFAULT_BLOCK_POS_TAG);
                for (int i = 0; i < pathNBT.tagCount(); i++) {
                    indexList.add(NBTUtils.readBlockPos(pathNBT.get(i)));
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
        NBTTagCompound cpt = NBTUtils.getItemDataOrNull(item);
        return cpt == null ? "" : cpt.getString("name");
    }

    @Override
    public boolean setName(ItemStack item, String name) {
        NBTTagCompound cpt = NBTUtils.getItemData(item);
        cpt.setString("name", name);
        return true;
    }

    @Override
    public void registerModels() {
        ModelHelper.registerItemModel(this, MapLocationType.CLEAN.meta, "/clean");
        ModelHelper.registerItemModel(this, MapLocationType.SPOT.meta, "/spot");
        ModelHelper.registerItemModel(this, MapLocationType.AREA.meta, "/area");
        ModelHelper.registerItemModel(this, MapLocationType.PATH.meta, "/path");
        ModelHelper.registerItemModel(this, MapLocationType.ZONE.meta, "/zone");
    }
}
